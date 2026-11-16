import json
import pandas as pd


TIMESTAMP_COLUMNS = {
    "CLAIMS": ["created_at", "updated_at"],
    "FRAUD_RULE_CONFIGURATIONS": ["created_at", "updated_at"],
    "CLAIM_FLAGS": ["created_at"],
    "ENTITY_MATCHES": ["created_at"],
    "EVIDENCE": ["uploaded_at"],
    "REPAIR_INVOICES": ["created_at"],
    "INVESTIGATIONS": [
        "started_at",
        "completed_at",
        "created_at",
        "updated_at",
    ],
    "INVESTIGATION_NOTES": ["created_at"],
    "EVIDENCE_REQUESTS": [
        "requested_at",
        "due_at",
        "fulfilled_at",
    ],
    "CLAIM_DECISIONS": ["decided_at"],
}


def serialize_json(value):
    """Convert PostgreSQL JSONB values into JSON strings."""

    if value is None:
        return None

    if isinstance(value, float) and pd.isna(value):
        return None

    if isinstance(value, str):
        return value

    try:
        return json.dumps(
            value,
            default=str,
            ensure_ascii=False,
        )
    except (TypeError, ValueError):
        return str(value)


def clean_dataframe(dataframe, table_name):
    """Apply common cleaning to an extracted operational table."""

    dataframe = dataframe.copy()

    if "id" in dataframe.columns:
        dataframe = dataframe.drop_duplicates(
            subset=["id"],
            keep="last",
        )

    for column in TIMESTAMP_COLUMNS.get(table_name, []):

        if column in dataframe.columns:
            dataframe[column] = pd.to_datetime(
                dataframe[column],
                errors="coerce",
                utc=True,
            )

    
    if (
        table_name == "ENTITY_MATCHES"
        and "match_details" in dataframe.columns
    ):
        dataframe["match_details"] = (
            dataframe["match_details"].apply(serialize_json)
        )

   
    dataframe = dataframe.astype(object)
    dataframe = dataframe.where(
        pd.notna(dataframe),
        None,
    )

    return dataframe


def transform_claims(dataframe):
    """Clean claims and derive analytical columns and risk summary."""

    if dataframe.empty:
        print("No claim records available for transformation.")

        return (
            clean_dataframe(dataframe, "CLAIMS"),
            pd.DataFrame(
                columns=[
                    "calculated_risk_band",
                    "total_claims",
                    "total_claimed_amount",
                    "average_risk_score",
                ]
            ),
        )

    dataframe = dataframe.copy()

    dataframe = dataframe.drop_duplicates(
        subset=["id"],
        keep="last",
    )

    dataframe["incident_date"] = pd.to_datetime(
        dataframe["incident_date"],
        errors="coerce",
    ).dt.date

    dataframe["claimed_amount"] = pd.to_numeric(
        dataframe["claimed_amount"],
        errors="coerce",
    )

    dataframe["risk_score"] = pd.to_numeric(
        dataframe["risk_score"],
        errors="coerce",
    )

    if dataframe["claimed_amount"].isna().any():
        raise ValueError(
            "CLAIMS contains missing or invalid claimed_amount values."
        )

    if dataframe["risk_score"].isna().any():
        raise ValueError(
            "CLAIMS contains missing or invalid risk_score values."
        )

    if dataframe["incident_date"].isna().any():
        raise ValueError(
            "CLAIMS contains missing or invalid incident_date values."
        )

    dataframe["risk_score"] = dataframe["risk_score"].clip(0, 100)

    dataframe["calculated_risk_band"] = pd.cut(
        dataframe["risk_score"],
        bins=[-1, 19, 49, 100],
        labels=["LOW", "MEDIUM", "HIGH"],
    ).astype(object)

  
    dataframe["claim_amount_category"] = pd.cut(
        dataframe["claimed_amount"],
        bins=[-1, 10000, 50000, float("inf")],
        labels=["LOW", "MEDIUM", "HIGH"],
    ).astype(object)

    risk_summary = (
        dataframe.groupby(
            "calculated_risk_band",
            observed=False,
        )
        .agg(
            total_claims=("id", "count"),
            total_claimed_amount=("claimed_amount", "sum"),
            average_risk_score=("risk_score", "mean"),
        )
        .reset_index()
    )

    risk_summary["calculated_risk_band"] = (
        risk_summary["calculated_risk_band"].astype(str)
    )

    risk_summary = risk_summary.astype(object)
    risk_summary = risk_summary.where(
        pd.notna(risk_summary),
        None,
    )

    dataframe = clean_dataframe(
        dataframe,
        "CLAIMS",
    )

    print("\nClaims transformation completed.")
    print("Claims transformed:", len(dataframe))
    print("Risk summary rows:", len(risk_summary))

    return dataframe, risk_summary


def transform_all(extracted_data):
    """Transform every extracted operational dataset."""

    transformed_data = {}

    for table_name, dataframe in extracted_data.items():

        if table_name == "CLAIMS":
            transformed_claims, risk_summary = transform_claims(
                dataframe
            )

            transformed_data["CLAIMS"] = transformed_claims
            transformed_data["FRAUD_RISK_SUMMARY"] = risk_summary

        else:
            transformed_data[table_name] = clean_dataframe(
                dataframe,
                table_name,
            )

    print("\nTransformation summary:")

    for table_name, dataframe in transformed_data.items():
        print(f"{table_name}: {len(dataframe)} rows")

    return transformed_data


if __name__ == "__main__":

    from extract import extract_all

    try:
        extracted_data = extract_all()
        transformed_data = transform_all(extracted_data)

        print("\nTransformation completed successfully.")

    finally:
        from extract import engine
        engine.dispose()
