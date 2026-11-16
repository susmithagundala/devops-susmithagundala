import os
import pandas as pd

from dotenv import load_dotenv
from sqlalchemy import URL, create_engine, text

load_dotenv()


def create_postgres_engine():

    required_variables = [
        "DB_USERNAME",
        "DB_PASSWORD",
        "DB_NAME",
    ]

    missing_variables = [
        variable
        for variable in required_variables
        if not os.getenv(variable)
    ]

    if missing_variables:
        raise ValueError(
            "Missing PostgreSQL environment variables: "
            + ", ".join(missing_variables)
        )

    database_url = URL.create(
        drivername="postgresql+psycopg2",
        username=os.getenv("DB_USERNAME"),
        password=os.getenv("DB_PASSWORD"),
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "5432")),
        database=os.getenv("DB_NAME"),
    )

    return create_engine(
        database_url,
        pool_pre_ping=True,
    )


engine = create_postgres_engine()


TABLE_QUERIES = {

    "CLAIMS": """
        SELECT *
        FROM claims
        ORDER BY id
    """,

    "FRAUD_RULE_CONFIGURATIONS": """
        SELECT *
        FROM fraud_rule_configurations
        ORDER BY id
    """,

    "CLAIM_FLAGS": """
        SELECT *
        FROM claim_flags
        ORDER BY id
    """,

    "ENTITY_MATCHES": """
        SELECT *
        FROM entity_matches
        ORDER BY id
    """,

    "EVIDENCE": """
        SELECT *
        FROM evidence
        ORDER BY id
    """,

    "REPAIR_INVOICES": """
        SELECT *
        FROM repair_invoices
        ORDER BY id
    """,

    "INVESTIGATIONS": """
        SELECT *
        FROM investigations
        ORDER BY id
    """,

    "INVESTIGATION_NOTES": """
        SELECT *
        FROM investigation_notes
        ORDER BY id
    """,

    "EVIDENCE_REQUESTS": """
        SELECT *
        FROM evidence_requests
        ORDER BY id
    """,

    "CLAIM_DECISIONS": """
        SELECT *
        FROM claim_decisions
        ORDER BY id
    """,

}

def extract_table(connection, query, table_name):

    try:

        dataframe = pd.read_sql(
            text(query),
            connection,
        )

        print(
            f"{table_name} extraction successful!"
        )

        print(
            "Number of records:",
            len(dataframe),
        )

        return dataframe

    except Exception as error:

        print(
            f"{table_name} extraction failed:",
            error,
        )

        raise

def extract_single_table(table_name):

    if table_name not in TABLE_QUERIES:

        raise ValueError(
            f"Unknown table: {table_name}"
        )

    try:

        with engine.connect() as connection:

            return extract_table(
                connection,
                TABLE_QUERIES[table_name],
                table_name,
            )

    except Exception:

        raise



def extract_claims():

    return extract_single_table("CLAIMS")


def extract_fraud_rules():

    return extract_single_table(
        "FRAUD_RULE_CONFIGURATIONS"
    )


def extract_claim_flags():

    return extract_single_table("CLAIM_FLAGS")


def extract_all():

    extracted_data = {}

    try:

        with engine.connect().execution_options(
            isolation_level="REPEATABLE READ"
        ) as connection:

            with connection.begin():

                print("\nStarting PostgreSQL extraction...\n")

                for table_name, query in TABLE_QUERIES.items():

                    extracted_data[table_name] = extract_table(
                        connection,
                        query,
                        table_name,
                    )

        print("\nAll tables extracted successfully!")

        return extracted_data

    except Exception as error:

        print("\nExtraction process failed:", error)

        raise


if __name__ == "__main__":

    try:

        extracted_data = extract_all()

        print("\n========== EXTRACTION SUMMARY ==========\n")

        for table_name, dataframe in extracted_data.items():

            print(
                f"{table_name}: {len(dataframe)} records"
            )

        print("\nExtraction completed successfully!")

    finally:

        engine.dispose()
