import { Bell } from "lucide-react";

export default function Notification() {
  return (
    <div className="flex items-center justify-end space-x-4">
      <button
        className="relative bg-black text-white rounded-full p-4 shadow-lg hover:bg-white hover:text-black focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-white-500 cursor-pointer transition-colors"
        onClick={() => {
          alert(
            "Notifications clicked! This is under construction, here you will able to see the notifications."
          ); // Placeholder for notification action
        }}
      >
        <Bell className="h-7 w-7" />
        <span class="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-white bg-red-600 rounded-full transform translate-x-1/2 -translate-y-1/2">
          3
        </span>
      </button>
    </div>
  );
}
