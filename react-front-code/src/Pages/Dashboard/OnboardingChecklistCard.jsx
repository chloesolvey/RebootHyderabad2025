import { useState } from "react";
import { onboardingTasks } from "../../mockData";
import { CheckSquare, Clock } from "lucide-react";

const OnboardingChecklistCard = () => {
  const [tasks, setTasks] = useState(onboardingTasks);
  const completedTasks = tasks.filter((t) => t.completed).length;
  const totalTasks = tasks.length;
  const progress =
    totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
  const remainingTasks = totalTasks - completedTasks;

  const toggleTask = (id) => {
    setTasks(
      tasks.map((task) =>
        task.id === id ? { ...task, completed: !task.completed } : task
      )
    );
  };

  return (
    <div className="bg-white dark:bg-slate-800/50 p-6 rounded-xl shadow-sm">
      <h2 className="text-xl font-semibold mb-4">Your Onboarding Checklist</h2>

      <div className="flex items-center mb-4">
        <div className="w-full bg-slate-200 dark:bg-slate-700 rounded-full h-2.5">
          <div
            className="h-2.5 rounded-full"
            style={{ width: `${progress}%`, background: "#11b67a" }}
          ></div>
        </div>
        <span className="ml-4 font-semibold text-black-600 dark:text-white-400">
          {progress}%
        </span>
      </div>

      <p className="text-sm text-slate-500 dark:text-slate-400 mb-5">
        You have {remainingTasks} tasks remaining. Keep up the great work!
      </p>

      <ul className="space-y-3 overflow-y-auto h-40">
        {tasks.map(
          (
            task // Show first 3 tasks
          ) => (
            <li
              key={task.id}
              className="flex items-center justify-between p-3 rounded-lg hover:bg-green-50 dark:hover:bg-slate-700/50"
            >
              <div className="flex items-center">
                <button
                  onClick={() => toggleTask(task.id)}
                  className={`w-6 h-6 rounded-md border-2 flex items-center justify-center transition-all ${
                    task.completed
                      ? "bg-black border-black-500"
                      : "border-slate-300 dark:border-slate-500"
                  }`}
                >
                  {task.completed && (
                    <CheckSquare className="w-4 h-4 text-white bg-black" />
                  )}
                </button>
                <span
                  className={`ml-3 ${
                    task.completed ? "line-through text-slate-400" : ""
                  }`}
                >
                  {task.text}
                </span>
              </div>
              <div className="flex items-center text-sm text-slate-400 dark:text-slate-500">
                <Clock className="w-4 h-4 mr-1.5" />
                Day {task.day}
              </div>
            </li>
          )
        )}
      </ul>
    </div>
  );
};
export default OnboardingChecklistCard;
