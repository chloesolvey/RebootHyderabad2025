import ManagerCard from "../../Components/ManagerCard";
import Notification from "../../Components/Notification";
import { currentUser } from "../../mockData";
import OnboardingChecklistCard from "./OnboardingChecklistCard";
import QuickLinksCard from "./QuickLinksCard";

const Dashboard = () => {
  return (
    <div>
      <div className="flex item-center justify-between">
        <h1 className="text-4xl font-bold text-slate-800 dark:text-white">
          Welcome, {currentUser.name}!
        </h1>
        <Notification />
      </div>
      <p className="text-slate-500 dark:text-slate-400 mt-1 mb-8">
        Here's your onboarding summary for today.
      </p>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-8">
          <OnboardingChecklistCard />
          <QuickLinksCard />
        </div>
        <div className="lg:col-span-1">
          <ManagerCard />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
