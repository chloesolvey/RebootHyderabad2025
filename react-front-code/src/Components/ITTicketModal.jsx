import { useState } from "react";
import Modal from "./Modal";
import axios from "axios";
import { API_URL } from "./Chatbot/Chatbot";

const ITTicketModal = ({ closeModal, message, getItRequestDetails }) => {
  const [itrequestDetails, setItRequestDetails] = useState({
    category: "",
    subject: "",
    description: "",
  });
  return (
    <Modal title="Raise an IT Ticket" closeModal={closeModal}>
      <form
        className="space-y-4"
        onSubmit={async (e) => {
          e.preventDefault();
          try {
            const response = await axios.post(
              `${API_URL}/chat`,
              {
                message: `${message} with category: ${itrequestDetails.category}, summary: ${itrequestDetails.subject} and description: ${itrequestDetails.description}`,
                sessionId: `session_${Date.now()}`,
              },
              {
                headers: {
                  "Content-Type": "application/json",
                },
              }
            );
            if (response.data.message !== "") {
              getItRequestDetails({
                id: Date.now() + 1,
                sender: "bot",
                text: response.data.response,
                link: "https://www.servicenow.com/",
                linkName: "ServiceNow",
              });
            }
          } catch (error) {
            console.error("Error raising IT ticket:", error);
            getItRequestDetails({
              sender: "bot",
              text: "Sorry, something went wrong.",
            });
          }
          closeModal();
        }}
      >
        <div>
          <label
            htmlFor="category"
            className="block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            Category
          </label>
          <select
            id="category"
            className="mt-1 block w-full rounded-md border-slate-300 dark:bg-slate-700 dark:border-slate-600 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 px-3 py-2"
            onChange={(e) => {
              setItRequestDetails({
                ...itrequestDetails,
                category: e.target.value,
              });
            }}
          >
            <option>Please select issue type</option>
            <option value="Hardware Issue">Hardware Issue</option>
            <option value="Software Access">Software Access</option>
            <option value="Network Problem">Network Problem</option>
            <option value="Account/Login issue">Account/Login Issue</option>
          </select>
        </div>
        <div>
          <label
            htmlFor="subject"
            className="block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            Subject
          </label>
          <input
            type="text"
            id="subject"
            placeholder="e.g., Cannot connect to VPN"
            className="mt-1 block w-full rounded-md border-slate-300 dark:bg-slate-700 dark:border-slate-600 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 px-3 py-2"
            onChange={(e) => {
              setItRequestDetails({
                ...itrequestDetails,
                subject: e.target.value,
              });
            }}
          />
        </div>
        <div>
          <label
            htmlFor="description"
            className="block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            Description
          </label>
          <textarea
            id="description"
            rows="4"
            placeholder="Please provide as much detail as possible..."
            className="mt-1 block w-full rounded-md border-slate-300 dark:bg-slate-700 dark:border-slate-600 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 px-3 py-2"
            onChange={(e) => {
              setItRequestDetails({
                ...itrequestDetails,
                description: e.target.value,
              });
            }}
          ></textarea>
        </div>
        <div className="flex justify-end pt-4">
          <button
            type="button"
            onClick={closeModal}
            className="bg-slate-200 dark:bg-slate-600 text-slate-800 dark:text-slate-200 font-semibold px-4 py-2 rounded-lg hover:bg-slate-300 dark:hover:bg-slate-500 mr-2 cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="bg-black text-white font-semibold px-4 py-2 rounded-lg hover:underline transition-colors duration-300 cursor-pointer"
            disabled={
              itrequestDetails.category==="" ||
              itrequestDetails.subject==="" ||
              itrequestDetails.description===""
            }
          >
            Submit Ticket
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default ITTicketModal;
