import os
import snowflake.connector
import time
import uuid

from dotenv import load_dotenv
from snowflake.connector.pandas_tools import write_pandas

from extract import extract_all, engine
from transform import transform_all


load_dotenv()


TABLE_KEYS = {
    "CLAIMS": "ID",
    "FRAUD_RULE_CONFIGURATIONS": "ID",
    "CLAIM_FLAGS": "ID",
    "ENTITY_MATCHES": "ID",
    "EVIDENCE": "ID",
    "REPAIR_INVOICES": "ID",
    "INVESTIGATIONS": "ID",
    "INVESTIGATION_NOTES": "ID",
    "EVIDENCE_REQUESTS": "ID",
    "CLAIM_DECISIONS": "ID",
    "FRAUD_RISK_SUMMARY": "CALCULATED_RISK_BAND",
}

LOAD_ORDER = [
    "CLAIMS",
    "FRAUD_RULE_CONFIGURATIONS",
    "CLAIM_FLAGS",
    "ENTITY_MATCHES",
    "EVIDENCE",
    "REPAIR_INVOICES",
    "INVESTIGATIONS",
    "INVESTIGATION_NOTES",
    "EVIDENCE_REQUESTS",
    "CLAIM_DECISIONS",
    "FRAUD_RISK_SUMMARY",
]


def get_snowflake_connection():
    """Create and validate a Snowflake connection."""

    config = {
        "account": os.getenv("SNOWFLAKE_ACCOUNT"),
        "user": os.getenv("SNOWFLAKE_USER"),
        "password": os.getenv("SNOWFLAKE_PASSWORD"),
        "warehouse": os.getenv("SNOWFLAKE_WAREHOUSE"),
        "database": os.getenv("SNOWFLAKE_DATABASE"),
        "schema": os.getenv("SNOWFLAKE_SCHEMA"),
    }

    role = os.getenv("SNOWFLAKE_ROLE")

    if role:
        config["role"] = role

    missing = [
        key
        for key, value in config.items()
        if not value
    ]

    if missing:
        raise ValueError(
            "Missing Snowflake configuration: "
            + ", ".join(missing)
        )

    return snowflake.connector.connect(**config)


def create_staging_table(connection, target_table):
    """Create a temporary staging table matching the destination."""

    staging_table = f"STG_{target_table}"
    cursor = connection.cursor()

    try:
        cursor.execute(
            f"""
            CREATE OR REPLACE TEMPORARY TABLE
            {staging_table}
            LIKE {target_table}
            """
        )
    finally:
        cursor.close()

    return staging_table


def upload_to_staging(connection, dataframe, target_table):
    """Upload a complete source DataFrame into its staging table."""

    if dataframe is None:
        raise ValueError(
            f"No DataFrame supplied for {target_table}."
        )

    staging_table = create_staging_table(
        connection,
        target_table,
    )

    upload_dataframe = dataframe.copy()

    upload_dataframe.columns = [
        str(column).upper()
        for column in upload_dataframe.columns
    ]

    if upload_dataframe.columns.duplicated().any():
        raise ValueError(
            f"Duplicate columns found in {target_table}."
        )

    if upload_dataframe.empty:
        print(
            f"{target_table}: source is empty; "
            "empty staging table created after safety validation."
        )
        return staging_table

    database = os.getenv("SNOWFLAKE_DATABASE")
    schema = os.getenv("SNOWFLAKE_SCHEMA")

    success, chunks, rows, output = write_pandas(
        conn=connection,
        df=upload_dataframe,
        table_name=staging_table,
        database=database,
        schema=schema,
        auto_create_table=False,
        overwrite=False,
        quote_identifiers=False,
        use_logical_type=True,
    )

    if not success:
        raise RuntimeError(
            f"Staging upload failed for {target_table}: {output}"
        )

    print(
        f"{target_table}: staged {rows} rows "
        f"in {chunks} chunk(s)."
    )

    return staging_table


def merge_and_reconcile(
    connection,
    dataframe,
    target_table,
):
    """
    Merge source records into the destination.

    After the MERGE, remove destination records that are absent
    from the complete PostgreSQL source snapshot.

    This makes the Snowflake table match the current source table.
    """

    if target_table not in TABLE_KEYS:
        raise ValueError(
            f"No primary-key configuration for {target_table}."
        )

    if dataframe is None:
        raise ValueError(
            f"No DataFrame supplied for {target_table}."
        )

    key_column = TABLE_KEYS[target_table]

    # Normalize and validate source column names.
    validation_df = dataframe.copy()

    validation_df.columns = [
        str(column).upper()
        for column in validation_df.columns
    ]

    if validation_df.columns.duplicated().any():
        raise ValueError(
            f"Duplicate columns found in {target_table}."
        )

    if key_column not in validation_df.columns:
        raise ValueError(
            f"{key_column} is missing from {target_table}."
        )

    # Primary keys must never be NULL.
    if validation_df[key_column].isna().any():
        raise ValueError(
            f"NULL primary keys found in {target_table}."
        )

    # Primary keys must be unique for a deterministic MERGE.
    if validation_df[key_column].duplicated().any():
        raise ValueError(
            f"Duplicate primary keys found in {target_table}."
        )

    # Prevent an empty source snapshot from deleting populated
    # Snowflake destination tables during reconciliation.
    if validation_df.empty:
        cursor = connection.cursor()

        try:
            cursor.execute(
                f"SELECT COUNT(*) FROM {target_table}"
            )

            destination_count = cursor.fetchone()[0]

        finally:
            cursor.close()

        if destination_count > 0:
            raise RuntimeError(
                f"SAFETY CHECK FAILED: {target_table} has "
                f"{destination_count} existing Snowflake rows, "
                "but the PostgreSQL source DataFrame is empty. "
                "Reconciliation stopped to prevent data deletion."
            )

    staging_table = upload_to_staging(
        connection,
        validation_df,
        target_table,
    )

    source_columns = list(validation_df.columns)

    columns_sql = ", ".join(source_columns)

    update_columns = [
        column
        for column in source_columns
        if column not in [key_column, "ETL_LOADED_AT"]
    ]

    if update_columns:
        update_sql = ",\n".join(
            f"target.{column} = source.{column}"
            for column in update_columns
        )

        update_sql += (
            ",\ntarget.ETL_LOADED_AT = CURRENT_TIMESTAMP()"
        )

        matched_clause = f"""
            WHEN MATCHED THEN UPDATE SET
                {update_sql}
        """
    else:
        matched_clause = ""

    insert_columns = source_columns.copy()

    if "ETL_LOADED_AT" not in insert_columns:
        insert_columns.append("ETL_LOADED_AT")

    insert_columns_sql = ", ".join(insert_columns)

    insert_values = [
        f"source.{column}"
        for column in source_columns
    ]

    if "ETL_LOADED_AT" not in source_columns:
        insert_values.append("CURRENT_TIMESTAMP()")

    insert_values_sql = ", ".join(insert_values)

    merge_sql = f"""
        MERGE INTO {target_table} AS target
        USING {staging_table} AS source
        ON target.{key_column} = source.{key_column}
        {matched_clause}
        WHEN NOT MATCHED THEN INSERT (
            {insert_columns_sql}
        )
        VALUES (
            {insert_values_sql}
        )
    """

    cursor = connection.cursor()

    try:
        # Merge inserted and updated records.
        cursor.execute(merge_sql)

        print(
            f"{target_table}: MERGE completed. "
            f"Affected rows: {cursor.rowcount}"
        )

        # Delete destination rows absent from the complete source snapshot.
        # An empty source can reach this point only when the destination
        # was already empty, as verified by the safety check above.
        delete_sql = f"""
            DELETE FROM {target_table} AS target
            WHERE NOT EXISTS (
                SELECT 1
                FROM {staging_table} AS source
                WHERE source.{key_column} = target.{key_column}
            )
        """

        cursor.execute(delete_sql)

        print(
            f"{target_table}: stale rows removed: "
            f"{cursor.rowcount}"
        )

    finally:
        cursor.close()


def refresh_claim_trends(connection):
    """Rebuild monthly claim trend analytics."""

    sql = """
        CREATE OR REPLACE TABLE CLAIM_TRENDS
        COPY GRANTS AS

        SELECT
            DATE_TRUNC(
                'MONTH',
                INCIDENT_DATE
            )::DATE AS CLAIM_MONTH,

            COUNT(*) AS TOTAL_CLAIMS,

            SUM(CLAIMED_AMOUNT) AS TOTAL_CLAIMED_AMOUNT,

            COUNT_IF(
                CALCULATED_RISK_BAND = 'LOW'
            ) AS LOW_RISK_CLAIMS,

            COUNT_IF(
                CALCULATED_RISK_BAND = 'MEDIUM'
            ) AS MEDIUM_RISK_CLAIMS,

            COUNT_IF(
                CALCULATED_RISK_BAND = 'HIGH'
            ) AS HIGH_RISK_CLAIMS,

            AVG(RISK_SCORE) AS AVERAGE_RISK_SCORE,

            CURRENT_TIMESTAMP() AS ETL_LOADED_AT

        FROM CLAIMS

        GROUP BY DATE_TRUNC(
            'MONTH',
            INCIDENT_DATE
        )::DATE

        ORDER BY CLAIM_MONTH
    """

    cursor = connection.cursor()

    try:
        cursor.execute(sql)
        print("CLAIM_TRENDS refreshed successfully.")

    finally:
        cursor.close()


def validate_destination_counts(connection, transformed_data):
    """Compare source DataFrame counts with Snowflake counts."""

    cursor = connection.cursor()

    try:
        print("\nDestination validation:")

        for table_name in LOAD_ORDER:

            cursor.execute(
                f"SELECT COUNT(*) FROM {table_name}"
            )

            destination_count = cursor.fetchone()[0]

            source_count = len(
                transformed_data[table_name]
            )

            status = (
                "MATCH"
                if source_count == destination_count
                else "MISMATCH"
            )

            print(
                f"{table_name}: "
                f"source={source_count}, "
                f"snowflake={destination_count}, "
                f"status={status}"
            )

            if source_count != destination_count:
                raise RuntimeError(
                    f"Count validation failed for {table_name}."
                )

    finally:
        cursor.close()


def ensure_etl_run_log_table(connection):
    """Create the ETL run log table if it does not already exist."""

    sql = """
        CREATE TABLE IF NOT EXISTS ETL_RUN_LOG (
            RUN_ID VARCHAR(36) NOT NULL,
            STARTED_AT TIMESTAMP_TZ,
            COMPLETED_AT TIMESTAMP_TZ,
            STATUS VARCHAR(20),
            EXTRACTED_ROWS NUMBER(38, 0) DEFAULT 0,
            TRANSFORMED_ROWS NUMBER(38, 0) DEFAULT 0,
            LOADED_ROWS NUMBER(38, 0) DEFAULT 0,
            ERROR_MESSAGE VARCHAR(4000),
            DURATION_SECONDS NUMBER(18, 3)
        )
    """

    cursor = connection.cursor()

    try:
        cursor.execute(sql)
    finally:
        cursor.close()


def start_etl_run(connection, run_id):
    """Insert a RUNNING record before ETL processing begins."""

    sql = """
        INSERT INTO ETL_RUN_LOG (
            RUN_ID,
            STARTED_AT,
            STATUS,
            EXTRACTED_ROWS,
            TRANSFORMED_ROWS,
            LOADED_ROWS
        )
        VALUES (
            %s,
            CURRENT_TIMESTAMP(),
            'RUNNING',
            0,
            0,
            0
        )
    """

    cursor = connection.cursor()

    try:
        cursor.execute(sql, (run_id,))
    finally:
        cursor.close()


def finish_etl_run(
    connection,
    run_id,
    status,
    extracted_rows,
    transformed_rows,
    loaded_rows,
    duration_seconds,
    error_message=None,
):
    """Update the ETL run with its final status and metrics."""

    sql = """
        UPDATE ETL_RUN_LOG
        SET
            COMPLETED_AT = CURRENT_TIMESTAMP(),
            STATUS = %s,
            EXTRACTED_ROWS = %s,
            TRANSFORMED_ROWS = %s,
            LOADED_ROWS = %s,
            ERROR_MESSAGE = %s,
            DURATION_SECONDS = %s
        WHERE RUN_ID = %s
    """

    cursor = connection.cursor()

    try:
        cursor.execute(
            sql,
            (
                status,
                extracted_rows,
                transformed_rows,
                loaded_rows,
                error_message[:4000] if error_message else None,
                duration_seconds,
                run_id,
            ),
        )
    finally:
        cursor.close()


def run_etl():
    snowflake_connection = None

    run_id = str(uuid.uuid4())
    started_at = time.perf_counter()

    extracted_rows = 0
    transformed_rows = 0
    loaded_rows = 0

    try:
        print("=" * 60)
        print("CLAIMSURE FRAUD ANALYTICS ETL")
        print(f"RUN ID: {run_id}")
        print("=" * 60)

        # Connect first so extraction and transformation failures
        # can also be recorded in Snowflake.
        print("\nSTEP 1: CONNECT TO SNOWFLAKE")

        snowflake_connection = get_snowflake_connection()

        ensure_etl_run_log_table(snowflake_connection)
        start_etl_run(snowflake_connection, run_id)

        print("ETL run registered with status RUNNING.")

        print("\nSTEP 2: EXTRACT")

        extracted_data = extract_all()

        extracted_rows = sum(
            len(dataframe)
            for dataframe in extracted_data.values()
        )

        print(f"Total extracted rows: {extracted_rows}")

        print("\nSTEP 3: TRANSFORM")

        transformed_data = transform_all(extracted_data)

        missing_tables = [
            table
            for table in LOAD_ORDER
            if table not in transformed_data
        ]

        if missing_tables:
            raise ValueError(
                "Missing transformed datasets: "
                + ", ".join(missing_tables)
            )

        # Validate every expected dataset before loading.
        for table_name in LOAD_ORDER:
            dataframe = transformed_data[table_name]

            if dataframe is None or not hasattr(dataframe, "columns"):
                raise TypeError(
                    f"Invalid transformed dataset for {table_name}; "
                    "expected a pandas DataFrame."
                )

        transformed_rows = sum(
            len(transformed_data[table_name])
            for table_name in LOAD_ORDER
        )

        print(f"Total transformed rows: {transformed_rows}")

        print("\nSTEP 4: LOAD AND RECONCILE")

        for table_name in LOAD_ORDER:
            print(f"\nLoading {table_name}...")

            merge_and_reconcile(
                snowflake_connection,
                transformed_data[table_name],
                table_name,
            )

            # Count a dataset only after its merge and reconciliation
            # function completes successfully.
            loaded_rows += len(transformed_data[table_name])

        print(f"\nTotal successfully loaded rows: {loaded_rows}")

        print("\nSTEP 5: REFRESH ANALYTICS")

        refresh_claim_trends(snowflake_connection)

        print("\nSTEP 6: VALIDATE")

        validate_destination_counts(
            snowflake_connection,
            transformed_data,
        )

        duration_seconds = round(
            time.perf_counter() - started_at,
            3,
        )

        finish_etl_run(
            connection=snowflake_connection,
            run_id=run_id,
            status="SUCCESS",
            extracted_rows=extracted_rows,
            transformed_rows=transformed_rows,
            loaded_rows=loaded_rows,
            duration_seconds=duration_seconds,
        )

        print("\n" + "=" * 60)
        print("ETL PIPELINE COMPLETED SUCCESSFULLY")
        print(f"RUN ID: {run_id}")
        print(f"DURATION: {duration_seconds} seconds")
        print("=" * 60)

    except Exception as error:
        duration_seconds = round(
            time.perf_counter() - started_at,
            3,
        )

        print("\nETL PIPELINE FAILED:")
        print(error)

        # Record the failure if the Snowflake run record was created.
        if snowflake_connection:
            try:
                finish_etl_run(
                    connection=snowflake_connection,
                    run_id=run_id,
                    status="FAILED",
                    extracted_rows=extracted_rows,
                    transformed_rows=transformed_rows,
                    loaded_rows=loaded_rows,
                    duration_seconds=duration_seconds,
                    error_message=str(error),
                )

                print("ETL failure recorded in ETL_RUN_LOG.")

            except Exception as logging_error:
                print(
                    "WARNING: Could not update ETL_RUN_LOG: "
                    f"{logging_error}"
                )

        raise

    finally:
        if snowflake_connection:
            snowflake_connection.close()

        engine.dispose()


if __name__ == "__main__":
    run_etl()

