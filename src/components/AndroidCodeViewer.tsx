import React, { useState } from 'react';
import { FolderCode, X, Copy, Check, Download } from 'lucide-react';
import { ANDROID_FILES, AndroidFile } from '../data/androidSourceFiles';

interface AndroidCodeViewerProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AndroidCodeViewer: React.FC<AndroidCodeViewerProps> = ({ isOpen, onClose }) => {
  const [selectedFile, setSelectedFile] = useState<AndroidFile>(ANDROID_FILES[4]); // FileDatabaseManager.kt by default
  const [copied, setCopied] = useState(false);

  if (!isOpen) return null;

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.content);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadFile = () => {
    const blob = new Blob([selectedFile.content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = selectedFile.name;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/80 backdrop-blur-sm select-none">
      <div className="relative w-full max-w-5xl h-[88vh] bg-[#17212b] rounded-2xl border border-slate-700/60 shadow-2xl flex flex-col overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Header */}
        <div className="px-5 py-3.5 bg-[#242f3d] border-b border-[#101419] flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-emerald-500/20 text-emerald-400">
              <FolderCode className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-white">Native Android Source Code</h2>
              <p className="text-xs text-slate-400">
                Jetpack Compose + File-Based Persistence + Firebase Realtime Database
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleCopy}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#242f3d] hover:bg-[#2d3a4b] text-slate-200 border border-slate-600/50 text-xs font-medium cursor-pointer transition-colors"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? 'Copied!' : 'Copy Code'}</span>
            </button>
            <button
              onClick={handleDownloadFile}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium cursor-pointer transition-colors"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Download File</span>
            </button>
            <button
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-lg cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
          {/* File Explorer Tree */}
          <div className="w-full md:w-72 bg-[#141b24] border-r border-[#101419] flex flex-col shrink-0">
            <div className="px-3 py-2 text-[11px] font-semibold text-slate-400 uppercase tracking-wider border-b border-[#101419]">
              Project Files ({ANDROID_FILES.length})
            </div>
            <div className="flex-1 overflow-y-auto p-2 space-y-1">
              {ANDROID_FILES.map((file) => {
                const isSelected = file.path === selectedFile.path;
                return (
                  <button
                    key={file.path}
                    onClick={() => setSelectedFile(file)}
                    className={`w-full text-left px-3 py-2 rounded-lg text-xs transition-colors cursor-pointer flex flex-col gap-0.5 ${
                      isSelected
                        ? 'bg-[#242f3d] text-emerald-400 font-medium border border-emerald-500/40'
                        : 'text-slate-300 hover:bg-[#1a232e]'
                    }`}
                  >
                    <span className="font-mono truncate">{file.name}</span>
                    <span className="text-[10px] text-slate-500 font-mono truncate">{file.path}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Code Viewer */}
          <div className="flex-1 flex flex-col bg-[#0d1219] overflow-hidden">
            <div className="px-4 py-2 bg-[#17212b]/60 border-b border-[#101419] flex items-center justify-between text-xs text-slate-400 font-mono">
              <span className="text-emerald-400">{selectedFile.path}</span>
              <span>{selectedFile.content.split('\n').length} lines</span>
            </div>
            <div className="flex-1 overflow-auto p-4 font-mono text-[12px] text-slate-200">
              <pre className="leading-relaxed">
                {selectedFile.content.split('\n').map((line, i) => (
                  <div key={i} className="table-row">
                    <span className="table-cell select-none pr-4 text-right text-slate-600 font-mono text-[11px]">
                      {i + 1}
                    </span>
                    <span className="table-cell">{line}</span>
                  </div>
                ))}
              </pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
