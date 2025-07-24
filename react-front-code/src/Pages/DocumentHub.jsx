import { documents } from "../mockData";

const DocumentHub = () => {
  return (
    <div>
      <h1 className="text-3xl font-bold mb-6">Document Hub</h1>
      <div className="bg-white dark:bg-slate-800/50 p-6 rounded-lg shadow-sm">
        <ul className="divide-y divide-slate-200 dark:divide-slate-700">
          {documents.map((doc) => (
            <li key={doc.id} className="flex items-center justify-between py-4">
              <div>
                <p className="font-semibold">{doc.name}</p>
                <span className="text-xs bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-medium px-2 py-0.5 rounded-full">
                  {doc.category}
                </span>
              </div>
              <a
                href="/example.docx"
                target="_blank"
                download={`${doc.name.split(" ").join("_")}.docx`}
                rel="noopener noreferrer"
                className="dark:text-green-400 hover:underline font-semibold hover:underline"
                style={{ color: "#11b67a" }}
              >
                View
              </a>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default DocumentHub;
