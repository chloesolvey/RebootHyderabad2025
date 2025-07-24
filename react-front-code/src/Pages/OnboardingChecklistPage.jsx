import { useState } from "react";
import { onboardingTasks } from "../mockData";

const OnboardingChecklistPage = () => {
    const [tasks, setTasks] = useState(onboardingTasks);

    const toggleTask = (id) => {
        setTasks(tasks.map(task => task.id === id ? { ...task, completed: !task.completed } : task));
    };

    return (
        <div>
            <h1 className="text-3xl font-bold mb-6">Your Onboarding Checklist</h1>
            <div className="bg-white dark:bg-slate-800/50 p-6 rounded-lg shadow-sm">
                <ul className="space-y-4">
                    {tasks.map(task => (
                        <li key={task.id} className="flex items-center p-3 rounded-md transition-colors hover:bg-slate-50 dark:hover:bg-slate-700/50">
                            <input
                                type="checkbox"
                                id={`task-${task.id}`}
                                checked={task.completed}
                                onChange={() => toggleTask(task.id)}
                                className="h-5 w-5 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500 cursor-pointer"
                            />
                            <label htmlFor={`task-${task.id}`} className={`ml-4 text-md flex-1 cursor-pointer ${task.completed ? 'line-through text-slate-500' : ''}`}>
                                {task.text}
                            </label>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default OnboardingChecklistPage