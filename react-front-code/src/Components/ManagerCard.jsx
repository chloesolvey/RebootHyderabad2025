import { currentUser, teamMembers } from "../mockData";

const ManagerCard = () => (
  <div className="bg-white dark:bg-slate-800/50 p-6 rounded-xl shadow-sm text-center">
    <h2 className="text-xl font-semibold mb-4 text-center">Your Manager</h2>
    <div className="w-24 h-24 bg-green-100 dark:bg-green-500/20 rounded-full mx-auto flex items-center justify-center mb-4">
      <span className="text-4xl font-bold text-black-600">
        {currentUser.managerInitials}
      </span>
    </div>
    <h3 className="text-lg font-semibold">{currentUser.manager}</h3>
    <p className="text-slate-500 dark:text-slate-400 text-sm mb-6">
      Engineering Manager
    </p>
    <a
      href={`mailto:${
        teamMembers.find((m) => m.name === currentUser.manager)?.email
      }`}
      className="w-full bg-black text-white font-semibold px-5 py-3 rounded-lg hover:bg-black transition-colors flex items-center justify-center hover:underline"
    >
      Send an Email
    </a>
  </div>
);

export default ManagerCard;
