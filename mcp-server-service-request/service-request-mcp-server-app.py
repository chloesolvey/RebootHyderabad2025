from fastapi import FastAPI
from pydantic import BaseModel
import random
import datetime

app = FastAPI(title="Mock Service Request MCP Server")

# Request model
class ServiceRequest(BaseModel):
    summary: str
    description: str

# Response model
class ServiceResponse(BaseModel):
    message: str
    ticket_id: str
    timestamp: str

# Core ticket creation logic
def create_ticket(description: str, summary: str) -> dict:
    print(f"--- Calling Jira API (mock) ---")

    prefix = "INC"
    number = random.randint(1000000, 9999999)
    ticket_id = f"{prefix}{number}"

    timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    return {
        "ticket_id": ticket_id,
        "timestamp": timestamp,
        "message": (
            f"Ticket Created with Ticket ID: {ticket_id} at {timestamp}.\n"
            f"Summary: {summary}\n"
            f"You can check the status in IT@LBG."
        )
    }

# Endpoint for creating a ticket
@app.post("/create_ticket", response_model=ServiceResponse)
def create_service_request(request: ServiceRequest):
    ticket_info = create_ticket(request.description, request.summary)
    return ServiceResponse(
        message=ticket_info["message"],
        ticket_id=ticket_info["ticket_id"],
        timestamp=ticket_info["timestamp"]
    )
