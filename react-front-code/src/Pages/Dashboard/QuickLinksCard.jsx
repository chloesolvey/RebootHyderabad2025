import { quickLinks } from "../../mockData";

const QuickLinksCard = () => (
  <div className="bg-white dark:bg-slate-800/50 p-6 rounded-xl shadow-sm">
    <h2 className="text-xl font-semibold mb-4">Quick Links</h2>
    <ul className="space-y-2 overflow-y-auto h-40">
      {quickLinks.map((link) => (
        <li key={link.id}>
          <a
            href={link.url}
            className="flex items-center justify-between p-3 rounded-lg text-black-600 dark:text-white-400 font-medium hover:bg-green-50 dark:hover:bg-slate-700/50"
          >
            <span>{link.name}</span>
            <span>&rarr;</span>
          </a>
        </li>
      ))}
    </ul>
  </div>
);

export default QuickLinksCard;
