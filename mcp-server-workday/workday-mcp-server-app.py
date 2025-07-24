from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional

# Initialize FastAPI app
app = FastAPI(title="Mock Workday Employee Info MCP")

# Mock employee data
MOCK_EMPLOYEE_DB = {
    "W12345": {
        "name": "Alice Johnson",
        "email": "alice.johnson@example.com",
        "title": "Senior Data Analyst",
        "manager": "Robert Lee",
        "location": "New York Office",
        "department": "Data & Analytics",
        "start_date": "2021-06-15"
    },
    "W98765": {
        "name": "Michael Chen",
        "email": "michael.chen@example.com",
        "title": "Software Engineer",
        "manager": "Alice Johnson",
        "location": "Remote",
        "department": "Engineering",
        "start_date": "2022-03-01"
    }
}

# Mock name to ID mapping
NAME_TO_ID = {
    "Alice Johnson": "W12345",
    "Michael Chen": "W98765"
}

# Request models
class EmployeeDetailsRequest(BaseModel):
    employee_id: str
    fields: Optional[List[str]] = None

class EmployeeIDRequest(BaseModel):
    name: str

# Routes
@app.post("/get_employee_details")
def get_employee_details(request: EmployeeDetailsRequest):
    employee = MOCK_EMPLOYEE_DB.get(request.employee_id)
    if not employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    
    if request.fields:
        filtered_data = {field: employee.get(field) for field in request.fields if field in employee}
        return filtered_data
    return employee

@app.post("/find_employee_id")
def find_employee_id(request: EmployeeIDRequest):
    employee_id = NAME_TO_ID.get(request.name)
    if not employee_id:
        raise HTTPException(status_code=404, detail="Employee ID not found for given name")
    return {"employee_id": employee_id}
