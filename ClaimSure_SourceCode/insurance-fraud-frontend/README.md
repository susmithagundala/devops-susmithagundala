# ClaimSure – Insurance Claim Fraud Triage & Evidence Ledger

React 18 + Vite frontend for the Spring Boot backend in this project. The UI uses Bootstrap 5 and Bootstrap Icons.

## Requirements
- Node.js 20 or later
- npm
- Spring Boot backend running at `http://127.0.0.1:8081`
- PostgreSQL data containing claimant profiles, active policies and covered vehicles
- AWS S3 configured in the backend for evidence uploads

## Run locally (Windows PowerShell)
1. Extract the `insurance-fraud-frontend` folder.
2. Open PowerShell in that folder.
3. Install dependencies:
   ```powershell
   npm install
   ```
4. Create the local environment file:
   ```powershell
   Copy-Item .env.example .env
   ```
5. Start Vite:
   ```powershell
   npm run dev
   ```
6. Open the local URL printed by Vite, normally `http://localhost:5173`.

The default API URL is `http://127.0.0.1:8081/api`. Change `VITE_API_BASE_URL` in `.env` only if your backend uses a different URL.

## Frontend features

### Claimant
- Register and sign in using the claimant authentication endpoints.
- View dashboard summaries and submitted claims.
- Submit a claim using policy and vehicle dropdowns populated from the signed-in claimant's own records.
- Optionally provide reported registration number and VIN for the backend's identifier consistency rule (R06).
- Upload evidence to an existing claim.
- Upload a repair invoice with invoice number, repairer name, optional GSTIN/phone, invoice date and amount.
- View pending evidence requests and submit evidence against a specific request.
- View and update customer profile details.

### Investigator
- Sign in using an administrator-provisioned investigator account.
- View dashboard counts.
- View flagged claims and their fraud rule indicators.
- Open a flagged claim as an investigation.
- View assigned investigations, entity matches, notes and evidence requests.
- Add investigation notes, request additional evidence and record a screening decision.

## Backend API routes used
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/claims`
- `GET /api/claims/{claimId}`
- `POST /api/claims`
- `GET /api/policies`
- `GET /api/policies/{policyId}/vehicles`
- `POST /api/claims/{claimId}/evidence`
- `GET /api/evidence/{evidenceId}`
- `GET /api/evidence/{evidenceId}/download`
- `GET /api/evidence-requests`
- `POST /api/evidence-requests/{requestId}/evidence`
- `GET /api/customers/me`
- `PUT /api/customers/me`
- `GET /api/dashboard/investigator`
- `GET /api/investigator/claims/flagged`
- `GET /api/investigator/investigations`
- `POST /api/investigator/claims/{claimId}/open`
- `GET /api/investigator/investigations/{id}`
- `GET /api/investigator/investigations/{id}/notes`
- `POST /api/investigator/investigations/{id}/notes`
- `GET /api/investigator/investigations/{id}/evidence-requests`
- `POST /api/investigator/investigations/{id}/evidence-requests`
- `POST /api/investigator/investigations/{id}/decision`
- `GET /api/investigator/claims/{claimId}/matches`

## Important integration notes
- Policy and vehicle dropdowns are populated from the authenticated claimant's policy records. The backend filters vehicles by policy ownership and active coverage dates.
- Repair invoice metadata is required by the backend when `evidenceType=REPAIR_INVOICE`.
- Evidence files are limited by the backend to PDF, JPEG, PNG or WebP and 10 MB.
- The investigator flagged queue lists claims with `UNDER_REVIEW` status and existing claim flags that do not yet have an investigation.
- The current investigator detail API returns claim summary, assigned investigator and investigation status, but does **not** return the claim's full fraud flag list or a list of uploaded evidence files. The UI displays entity matches, notes and evidence-request status that the current API exposes; it does not invent unavailable data.
- The current claimant evidence-request list returns pending requests only.
- The investigator decision API is called with `decision` and `reason`. The UI requires a reason for rejection and warns against deciding while requests are pending. Backend-side enforcement should also be tested.
- `CLEARED_FOR_PROCESSING` means the claim passed fraud screening; it is not final insurer approval or payment.

## Security notes
- JWT is stored in browser `localStorage` for this development version. Production deployment should receive a security review and consider an HttpOnly, Secure, SameSite cookie approach.
- Never commit `.env`, AWS credentials, database passwords or JWT secrets.
- Investigator accounts are provisioned by the backend/admin. Public investigator registration is not provided.

## Build
```powershell
npm run build
```
