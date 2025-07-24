import { X } from "lucide-react";

const Modal = ({ children, title, closeModal }) => (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white dark:bg-slate-800 rounded-lg shadow-xl w-full max-w-md">
            <header className="flex items-center justify-between p-4 border-b border-slate-200 dark:border-slate-700">
                <h2 className="text-lg font-semibold">{title}</h2>
                <button onClick={closeModal} className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 cursor-pointer">
                    <X className="h-6 w-6" />
                </button>
            </header>
            <div className="p-6">
                {children}
            </div>
        </div>
    </div>
);

export default Modal;