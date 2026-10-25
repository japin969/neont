import React, { useState } from 'react';
import { Menu, Search, HardDrive, FolderCode, Wifi, WifiOff, X } from 'lucide-react';

interface TelegramHeaderProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
  onMenuClick: () => void;
  onSearchChange: (query: string) => void;
  onOpenStorageInspector: () => void;
  onOpenCodeViewer: () => void;
  isOnlineNetwork: boolean;
  onToggleNetwork: () => void;
}

const TABS = [
  { id: 'all', label: 'All Chats' },
  { id: 'personal', label: 'Personal' },
  { id: 'groups', label: 'Groups' },
  { id: 'channels', label: 'Channels' }
];

export const TelegramHeader: React.FC<TelegramHeaderProps> = ({
  activeTab,
  onTabChange,
  onMenuClick,
  onSearchChange,
  onOpenStorageInspector,
  onOpenCodeViewer,
  isOnlineNetwork,
  onToggleNetwork
}) => {
  const [isSearching, setIsSearching] = useState(false);
  const [searchValue, setSearchValue] = useState('');

  const handleSearchInput = (val: string) => {
    setSearchValue(val);
    onSearchChange(val);
  };

  const clearSearch = () => {
    setSearchValue('');
    onSearchChange('');
    setIsSearching(false);
  };

  return (
    <header className="bg-[#242f3d] border-b border-[#101419] select-none sticky top-0 z-30 shadow-xs">
      {/* Primary Top Bar */}
      <div className="flex items-center justify-between px-3 h-14">
        {isSearching ? (
          <div className="flex items-center w-full gap-2">
            <Search className="w-5 h-5 text-slate-400 shrink-0" />
            <input
              type="text"
              autoFocus
              placeholder="Search chats, contacts, messages..."
              value={searchValue}
              onChange={(e) => handleSearchInput(e.target.value)}
              className="w-full bg-transparent text-white text-[15px] placeholder-slate-400 outline-none"
            />
            <button
              onClick={clearSearch}
              className="p-1 text-slate-400 hover:text-white cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        ) : (
          <>
            <div className="flex items-center gap-4">
              <button
                onClick={onMenuClick}
                className="p-2 -ml-1 text-slate-200 hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
                title="Open Navigation Drawer"
              >
                <Menu className="w-6 h-6" />
              </button>
              <div className="flex items-center gap-2">
                <h1 className="text-[19px] font-bold text-white tracking-tight">Neont</h1>
                {!isOnlineNetwork && (
                  <span className="text-[11px] px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-300 font-medium">
                    Offline
                  </span>
                )}
              </div>
            </div>

            {/* Quick Actions */}
            <div className="flex items-center gap-1">
              {/* Network Toggle Button */}
              <button
                onClick={onToggleNetwork}
                className={`p-2 rounded-full cursor-pointer transition-colors ${
                  isOnlineNetwork
                    ? 'text-emerald-400 hover:bg-slate-700/50'
                    : 'text-rose-400 bg-rose-950/40'
                }`}
                title={isOnlineNetwork ? 'Connected to Firebase. Click to simulate offline.' : 'Offline mode. Click to reconnect.'}
              >
                {isOnlineNetwork ? <Wifi className="w-5 h-5" /> : <WifiOff className="w-5 h-5" />}
              </button>

              {/* JSON Database Inspector */}
              <button
                onClick={onOpenStorageInspector}
                className="p-2 text-slate-300 hover:text-[#5288c1] hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
                title="View JSON File Database (/neont_db/*.json)"
              >
                <HardDrive className="w-5 h-5" />
              </button>

              {/* Kotlin Code Viewer */}
              <button
                onClick={onOpenCodeViewer}
                className="p-2 text-slate-300 hover:text-emerald-400 hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
                title="Explore Android Kotlin Source Files"
              >
                <FolderCode className="w-5 h-5" />
              </button>

              {/* Search Toggle */}
              <button
                onClick={() => setIsSearching(true)}
                className="p-2 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
                title="Search"
              >
                <Search className="w-5 h-5" />
              </button>
            </div>
          </>
        )}
      </div>

      {/* Telegram Folders / Tabs Bar */}
      <div className="flex items-center px-2 overflow-x-auto no-scrollbar gap-1">
        {TABS.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`relative px-4 py-2.5 text-[14px] font-medium whitespace-nowrap cursor-pointer transition-colors ${
                isActive ? 'text-[#5288c1]' : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {tab.label}
              {isActive && (
                <span className="absolute bottom-0 inset-x-2 h-[2.5px] bg-[#5288c1] rounded-t-full" />
              )}
            </button>
          );
        })}
      </div>
    </header>
  );
};
