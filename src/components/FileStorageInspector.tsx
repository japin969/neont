import React, { useState, useEffect } from 'react';
import { HardDrive, X, Download, FileCode, Activity, RefreshCw } from 'lucide-react';
import { fileDb } from '../services/fileDatabase';
import { FileIOLog } from '../types';

interface FileStorageInspectorProps {
  isOpen: boolean;
  onClose: () => void;
}

export const FileStorageInspector: React.FC<FileStorageInspectorProps> = ({ isOpen, onClose }) => {
  const [files, setFiles] = useState<{ fileName: string; sizeBytes: number; itemCount: number; raw: string }[]>([]);
  const [selectedFileName, setSelectedFileName] = useState<string>('users.json');
  const [logs, setLogs] = useState<FileIOLog[]>([]);

  const refreshFiles = () => {
    const list = fileDb.getAllVirtualFiles();
    setFiles(list);
    if (!list.some((f) => f.fileName === selectedFileName) && list.length > 0) {
      setSelectedFileName(list[0].fileName);
    }
  };

  useEffect(() => {
    if (isOpen) {
      refreshFiles();
    }
    const unsub = fileDb.onLog((log) => {
      setLogs((prev) => [log, ...prev].slice(0, 40));
      refreshFiles();
    });
    return () => unsub();
  }, [isOpen]);

  if (!isOpen) return null;

  const currentFile = files.find((f) => f.fileName === selectedFileName);

  const downloadAllJson = () => {
    const backup: Record<string, any> = {};
    files.forEach((f) => {
      try {
        backup[f.fileName] = JSON.parse(f.raw);
      } catch {
        backup[f.fileName] = f.raw;
      }
    });
    const blob = new Blob([JSON.stringify(backup, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `neont_internal_storage_backup_${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/80 backdrop-blur-sm select-none">
      <div className="relative w-full max-w-5xl h-[88vh] bg-[#17212b] rounded-2xl border border-slate-700/60 shadow-2xl flex flex-col overflow-hidden animate-in zoom-in-95 duration-150">
        {/* Modal Header */}
        <div className="px-5 py-3.5 bg-[#242f3d] border-b border-[#101419] flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-lg bg-[#4fa0ff]/20 text-[#4fa0ff]">
              <HardDrive className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-semibold text-white">File-Based Local Database</h2>
              <p className="text-xs font-mono text-slate-400">
                Location: <span className="text-[#4fa0ff]">context.filesDir/neont_db/</span>
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={refreshFiles}
              className="p-1.5 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-lg cursor-pointer transition-colors"
              title="Refresh"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
            <button
              onClick={downloadAllJson}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#5288c1] hover:bg-[#4076ad] text-white text-xs font-medium cursor-pointer transition-colors"
            >
              <Download className="w-3.5 h-3.5" />
              <span>Export Storage</span>
            </button>
            <button
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-lg cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Main Body: 3-column or split layout */}
        <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
          {/* File list sidebar */}
          <div className="w-full md:w-64 bg-[#141b24] border-r border-[#101419] flex flex-col shrink-0">
            <div className="px-3 py-2 text-[11px] font-semibold text-slate-400 uppercase tracking-wider border-b border-[#101419]">
              JSON Files ({files.length})
            </div>
            <div className="flex-1 overflow-y-auto p-2 space-y-1">
              {files.map((file) => {
                const isSelected = file.fileName === selectedFileName;
                return (
                  <button
                    key={file.fileName}
                    onClick={() => setSelectedFileName(file.fileName)}
                    className={`w-full flex items-center justify-between px-3 py-2 rounded-lg text-left text-xs transition-colors cursor-pointer ${
                      isSelected
                        ? 'bg-[#242f3d] text-white font-medium border border-[#5288c1]/40'
                        : 'text-slate-300 hover:bg-[#1a232e]'
                    }`}
                  >
                    <div className="flex items-center gap-2 min-w-0">
                      <FileCode className={`w-4 h-4 shrink-0 ${isSelected ? 'text-[#4fa0ff]' : 'text-slate-500'}`} />
                      <span className="truncate">{file.fileName}</span>
                    </div>
                    <span className="text-[10px] font-mono text-slate-400 shrink-0">
                      {(file.sizeBytes / 1024).toFixed(1)} KB
                    </span>
                  </button>
                );
              })}
            </div>

            {/* Architecture note */}
            <div className="p-3 bg-[#111720] border-t border-[#101419] text-[11px] text-slate-400 leading-normal">
              <span className="text-emerald-400 font-semibold block mb-0.5">Offline-First Engine</span>
              No SQLite / Room overhead. Every query is an in-memory Mutex-guarded atomic file write.
            </div>
          </div>

          {/* Center: File Viewer */}
          <div className="flex-1 flex flex-col bg-[#0f1721] overflow-hidden">
            <div className="px-4 py-2 bg-[#17212b]/60 border-b border-[#101419] flex items-center justify-between text-xs text-slate-400 font-mono">
              <span>{selectedFileName}</span>
              <span>{currentFile ? `${currentFile.itemCount} records (${currentFile.sizeBytes} bytes)` : ''}</span>
            </div>
            <div className="flex-1 overflow-auto p-4 font-mono text-[12px] text-slate-200">
              <pre className="whitespace-pre-wrap leading-relaxed">
                {currentFile ? currentFile.raw : '// File is empty'}
              </pre>
            </div>
          </div>

          {/* Right sidebar: Live I/O Log Stream */}
          <div className="w-full md:w-80 bg-[#121922] border-l border-[#101419] flex flex-col shrink-0">
            <div className="px-3 py-2 text-[11px] font-semibold text-slate-400 uppercase tracking-wider border-b border-[#101419] flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Activity className="w-3.5 h-3.5 text-amber-400" />
                Live Disk I/O Log
              </span>
              <span className="text-[10px] font-mono text-slate-500">Debounce: 300ms</span>
            </div>

            <div className="flex-1 overflow-y-auto p-2 space-y-2 font-mono text-[11px]">
              {logs.length === 0 ? (
                <p className="text-slate-500 text-center py-6">Waiting for disk operations...</p>
              ) : (
                logs.map((log) => (
                  <div
                    key={log.id}
                    className="p-2 rounded-lg bg-[#182330] border border-slate-800 flex flex-col gap-0.5 animate-in fade-in"
                  >
                    <div className="flex items-center justify-between">
                      <span
                        className={`font-bold px-1.5 py-0.2 rounded text-[10px] ${
                          log.operation === 'READ'
                            ? 'bg-blue-500/20 text-blue-400'
                            : log.operation === 'WRITE'
                            ? 'bg-emerald-500/20 text-emerald-400'
                            : log.operation === 'APPEND'
                            ? 'bg-purple-500/20 text-purple-400'
                            : log.operation === 'DEBOUNCE_FLUSH'
                            ? 'bg-amber-500/20 text-amber-400'
                            : 'bg-rose-500/20 text-rose-400'
                        }`}
                      >
                        {log.operation}
                      </span>
                      <span className="text-[10px] text-slate-500">{log.durationMs}ms</span>
                    </div>
                    <span className="text-slate-200 font-semibold truncate">{log.fileName}</span>
                    <span className="text-slate-400 text-[10px] break-all">{log.detail}</span>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
