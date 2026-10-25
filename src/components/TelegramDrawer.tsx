import React from 'react';
import {
  Users,
  UserCheck,
  Phone,
  Bookmark,
  Settings,
  UserPlus,
  HelpCircle,
  Moon,
  FolderCode,
  HardDrive,
  KeyRound,
  Wifi,
  WifiOff,
  X
} from 'lucide-react';
import { User } from '../types';
import { TelegramAvatar } from './TelegramAvatar';

interface TelegramDrawerProps {
  isOpen: boolean;
  user: User | null;
  isOnlineNetwork: boolean;
  onClose: () => void;
  onOpenSavedMessages: () => void;
  onOpenStorageInspector: () => void;
  onOpenCodeViewer: () => void;
  onOpenAuthModal: () => void;
  onToggleNetwork: () => void;
}

export const TelegramDrawer: React.FC<TelegramDrawerProps> = ({
  isOpen,
  user,
  isOnlineNetwork,
  onClose,
  onOpenSavedMessages,
  onOpenStorageInspector,
  onOpenCodeViewer,
  onOpenAuthModal,
  onToggleNetwork
}) => {
  if (!isOpen) return null;

  const displayName = user ? `${user.firstName} ${user.lastName}`.trim() : 'Guest User';
  const displayPhone = user?.phoneNumber || '+1 415 555 0199';
  const displayUsername = user?.username ? `@${user.username}` : '@neont_engineer';

  return (
    <div className="fixed inset-0 z-50 flex">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/60 backdrop-blur-[1px] transition-opacity"
        onClick={onClose}
      />

      {/* Drawer Body */}
      <div className="relative w-80 max-w-[85vw] h-full bg-[#17212b] shadow-2xl flex flex-col z-10 animate-in slide-in-from-left duration-200">
        {/* Top User Profile Header (Telegram signature deep blue header) */}
        <div className="p-4 bg-[#242f3d] border-b border-[#101419] flex flex-col justify-end">
          <div className="flex items-start justify-between mb-3">
            <TelegramAvatar
              name={displayName}
              avatarUrl={user?.avatarUrl}
              size={60}
            />
            <button
              onClick={onClose}
              className="p-1.5 rounded-full text-slate-400 hover:text-white hover:bg-slate-700/50 cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-[16px] font-semibold text-white tracking-tight leading-snug">
                {displayName}
              </h2>
              <p className="text-[13px] text-slate-400 font-normal">{displayPhone}</p>
              <p className="text-[12px] text-[#4fa0ff]">{displayUsername}</p>
            </div>
          </div>
        </div>

        {/* Navigation Items List */}
        <div className="flex-1 overflow-y-auto py-2 divide-y divide-[#101419]/50">
          <div className="py-1">
            <button
              onClick={onOpenSavedMessages}
              className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left"
            >
              <Bookmark className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">Saved Messages</span>
            </button>

            <button
              onClick={onOpenStorageInspector}
              className="w-full flex items-center justify-between px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left"
            >
              <div className="flex items-center gap-4">
                <HardDrive className="w-5 h-5 text-[#4fa0ff]" />
                <span className="text-[15px] font-medium">JSON File Database</span>
              </div>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-[#4fa0ff]/20 text-[#4fa0ff]">
                Internal Storage
              </span>
            </button>

            <button
              onClick={onOpenCodeViewer}
              className="w-full flex items-center justify-between px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left"
            >
              <div className="flex items-center gap-4">
                <FolderCode className="w-5 h-5 text-emerald-400" />
                <span className="text-[15px] font-medium">Android Kotlin Code</span>
              </div>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400">
                Ready to Build
              </span>
            </button>

            <button
              onClick={onOpenAuthModal}
              className="w-full flex items-center justify-between px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left"
            >
              <div className="flex items-center gap-4">
                <KeyRound className="w-5 h-5 text-amber-400" />
                <span className="text-[15px] font-medium">Firebase Auth</span>
              </div>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-amber-500/20 text-amber-400">
                {user ? 'Logged In' : 'Sign In'}
              </span>
            </button>
          </div>

          <div className="py-1">
            <button
              onClick={onToggleNetwork}
              className="w-full flex items-center justify-between px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left"
            >
              <div className="flex items-center gap-4">
                {isOnlineNetwork ? (
                  <Wifi className="w-5 h-5 text-emerald-400" />
                ) : (
                  <WifiOff className="w-5 h-5 text-rose-400" />
                )}
                <span className="text-[15px] font-medium">Network Sync</span>
              </div>
              <span
                className={`text-[12px] font-medium ${
                  isOnlineNetwork ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                {isOnlineNetwork ? 'Online' : 'Offline'}
              </span>
            </button>

            <div className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left">
              <Users className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">New Group</span>
            </div>

            <div className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left">
              <UserCheck className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">Contacts</span>
            </div>

            <div className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left">
              <Phone className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">Calls</span>
            </div>

            <div className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left">
              <Settings className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">Settings</span>
            </div>
          </div>

          <div className="py-1">
            <div className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left">
              <UserPlus className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">Invite Friends</span>
            </div>

            <div className="w-full flex items-center gap-4 px-5 py-3 text-slate-200 hover:bg-[#202b37] transition-colors cursor-pointer text-left">
              <HelpCircle className="w-5 h-5 text-slate-400" />
              <span className="text-[15px] font-medium">Telegram Features</span>
            </div>
          </div>
        </div>

        {/* Drawer Footer */}
        <div className="p-4 border-t border-[#101419] bg-[#121921] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Moon className="w-4 h-4 text-slate-400" />
            <span className="text-xs text-slate-400">Night Mode</span>
          </div>
          <span className="text-xs text-[#5288c1] font-medium">Neont Android v1.0</span>
        </div>
      </div>
    </div>
  );
};
