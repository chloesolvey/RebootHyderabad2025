import { Mail, Phone, User } from "lucide-react";
import { teamMembers } from "../mockData";

const TeamDirectory = () => (
  <div>
    <h1 className="text-3xl font-bold mb-6">Team Directory</h1>
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {teamMembers.map((member) => (
        <div
          key={member.id}
          className="bg-white dark:bg-slate-800/50 p-6 rounded-lg shadow-sm text-center"
        >
          <div className="w-24 h-24 bg-green-100 dark:bg-green-500/20 rounded-full mx-auto flex items-center justify-center mb-4">
            {member?.img ? (
              <img src={member.img} alt={member.name}/>
            ) : (
              <span className="text-4xl font-bold text-black-600">
                {member.initials}
              </span>
            )}
          </div>
          <h3 className="text-lg font-semibold">{member.name}</h3>
          <p
            className="dark:text-green-400 text-sm mb-4"
            style={{ color: "#11b67a" }}
          >
            {member.role}
          </p>
          <div className="text-left text-sm space-y-2 text-slate-600 dark:text-slate-300">
            <p className="flex items-center">
              <Mail className="h-4 w-4 mr-2 text-slate-400" /> {member.email}
            </p>
            <p className="flex items-center">
              <Phone className="h-4 w-4 mr-2 text-slate-400" /> {member.phone}
            </p>
          </div>
        </div>
      ))}
    </div>
  </div>
);

export default TeamDirectory;
