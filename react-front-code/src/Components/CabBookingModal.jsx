import { useState } from "react";
import Modal from "./Modal";
import axios from "axios";
import { API_URL } from "./Chatbot/Chatbot";

const CabBookingModal = ({ closeModal, getCabBookingDetails, message }) => {
  const [pickup, setPickup] = useState("");
  const [dropoff, setDropoff] = useState("");
  const [pickupTime, setPickupTime] = useState("");
  console.log("CabBookingModal message:", message);
  return (
    <Modal title="Book a Cab" closeModal={closeModal}>
      <form
        className="space-y-4"
        onSubmit={async (e) => {

          e.preventDefault();
          try {
            const response = await axios.post(
              `${API_URL}/chat`,
              {
                message: `${message} from ${pickup} to ${dropoff} for this time ${pickupTime}`,
                sessionId: `session_${Date.now()}`,
              },
              {
                headers: {
                  "Content-Type": "application/json",
                },
              }
            );
            if (response.data.message !== "") {
              getCabBookingDetails({
                id: Date.now() + 1,
                sender: "bot",
                text: response.data.response,
                link: "https://routematic.com/",
                linkName: "Routematic",
              });
            }
          } catch (error) {
            console.error("Error booking cab:", error);
            getCabBookingDetails({
              sender: "bot",
              text: "Sorry, something went wrong.",
            });
          }
          closeModal();
        }}
      >
        <div>
          <label
            htmlFor="pickup"
            className="block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            Pickup Location
          </label>
          <input
            type="text"
            id="pickup"
            placeholder="e.g., Knowledge Park, Hyderabad"
            value={pickup}
            onChange={(e) => {
              setPickup(e.target.value);
            }}
            className="mt-1 block w-full rounded-md border-slate-300 dark:bg-slate-700 dark:border-slate-600 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 px-3 py-2"
          />
        </div>
        <div>
          <label
            htmlFor="dropoff"
            className="block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            Drop-off Location
          </label>
          <input
            type="text"
            id="dropoff"
            placeholder="e.g., 123 Main St, Anytown"
            value={dropoff}
            onChange={(e) => {
              setDropoff(e.target.value);
            }}
            className="mt-1 block w-full rounded-md border-slate-300 dark:bg-slate-700 dark:border-slate-600 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 px-3 py-2"
          />
        </div>
        <div>
          <label
            htmlFor="time"
            className="block text-sm font-medium text-slate-700 dark:text-slate-300"
          >
            Pickup Time
          </label>
          <input
            type="datetime-local"
            id="time"
            value={pickupTime}
            onChange={(e) => {
              setPickupTime(e.target.value);
            }}
            className="mt-1 block w-full rounded-md border-slate-300 dark:bg-slate-700 dark:border-slate-600 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 px-3 py-2"
          />
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
            disabled={pickup === "" || dropoff === "" || pickupTime === ""}
          >
            Request Cab
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default CabBookingModal;
