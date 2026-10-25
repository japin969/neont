import React from 'react';
import { Check, CheckCheck, Clock, Mic, Pin, VolumeX } from 'lucide-react';
import { Chat } from '../types';
import { TelegramAvatar } from './TelegramAvatar';

interface ChatListItemProps {
  chat: Chat;
  isSelected?: boolean;
  isTyping?: boolean;
  onClick: () => void;
}

export const ChatListItem: React.FC<ChatListItemProps> = ({
  chat,
  isSelected = false,
  isTyping = false,
  onClick
}) => {
  const formatTime = (ts?: number) => {
    if (!ts) return '';
    const date = new Date(ts);
    const now = new Date();
    if (date.toDateString() === now.toDateString()) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });
    }
    return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
  };

  const isOutgoing = chat.lastMessage?.senderId === 'me';
  const status = chat.lastMessage?.status;

  return (
    <div
      onClick={onClick}
      className={`group flex items-center gap-3.5 px-4 py-2.5 cursor-pointer transition-colors select-none ${
        isSelected
          ? 'bg-[#242f3d]'
          : 'hover:bg-[#1f2936] active:bg-[#242f3d]/70'
      }`}
    >
      {/* Avatar */}
      <TelegramAvatar
        name={chat.title}
        avatarUrl={chat.avatarUrl}
        size={52}
        isOnline={chat.type === 'direct' && chat.title.includes('Pavel')}
      />

      {/* Middle: Title & Message preview */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between gap-2 mb-0.5">
          <h3 className="text-[15px] font-semibold text-slate-100 truncate tracking-tight">
            {chat.title}
          </h3>
          <span className="text-[12px] text-slate-400 shrink-0 font-normal">
            {formatTime(chat.lastMessage?.timestamp)}
          </span>
        </div>

        <div className="flex items-center justify-between gap-2">
          {/* Subtitle / Preview */}
          <div className="flex items-center gap-1.5 text-[14px] truncate flex-1">
            {isTyping ? (
              <span className="text-[#4fa0ff] flex items-center gap-1 font-medium animate-pulse">
                <span>typing</span>
                <span className="inline-flex gap-0.5">
                  <span className="w-1 h-1 rounded-full bg-[#4fa0ff] animate-bounce" />
                  <span className="w-1 h-1 rounded-full bg-[#4fa0ff] animate-bounce [animation-delay:0.2s]" />
                  <span className="w-1 h-1 rounded-full bg-[#4fa0ff] animate-bounce [animation-delay:0.4s]" />
                </span>
              </span>
            ) : (
              <>
                {/* Outgoing delivery tick on preview */}
                {isOutgoing && (
                  <span className="shrink-0 text-slate-400">
                    {status === 'pending' && <Clock className="w-3.5 h-3.5" />}
                    {status === 'sent' && <Check className="w-3.5 h-3.5 text-[#6ab2f2]" />}
                    {(status === 'read' || status === 'delivered') && (
                      <CheckCheck className="w-4 h-4 text-[#6ab2f2]" />
                    )}
                  </span>
                )}
                {chat.lastMessage?.type === 'voice' && (
                  <Mic className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                )}
                <span className="text-slate-400 truncate">
                  {chat.lastMessage?.text || 'No messages yet'}
                </span>
              </>
            )}
          </div>

          {/* Right badges: Mute, Pin, Unread */}
          <div className="flex items-center gap-1.5 shrink-0">
            {chat.isMuted && <VolumeX className="w-3.5 h-3.5 text-slate-500" />}
            {chat.isPinned && !chat.unreadCount && (
              <Pin className="w-3.5 h-3.5 text-slate-400 rotate-45" />
            )}
            {chat.unreadCount > 0 && (
              <span
                className={`min-w-[20px] h-[20px] px-1.5 rounded-full flex items-center justify-center text-[12px] font-bold text-white shadow-xs ${
                  chat.isMuted ? 'bg-[#38434f]' : 'bg-[#456b9a]'
                }`}
              >
                {chat.unreadCount}
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
