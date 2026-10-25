import React, { useState, useRef, useEffect } from 'react';
import {
  ArrowLeft,
  Phone,
  MoreVertical,
  Paperclip,
  Smile,
  Send,
  Mic,
  Check,
  CheckCheck,
  Clock,
  Play,
  Pause,
  Image,
  FileText,
  MapPin,
  BarChart2,
  X
} from 'lucide-react';
import { Chat, Message, User } from '../types';
import { TelegramAvatar } from './TelegramAvatar';

interface ChatScreenProps {
  chat: Chat;
  messages: Message[];
  currentUser: User | null;
  isTyping: boolean;
  onBack: () => void;
  onSendMessage: (text: string, type?: 'text' | 'voice', mediaUrl?: string) => void;
}

export const ChatScreen: React.FC<ChatScreenProps> = ({
  chat,
  messages,
  currentUser,
  isTyping,
  onBack,
  onSendMessage
}) => {
  const [inputText, setInputText] = useState('');
  const [showAttachMenu, setShowAttachMenu] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [isRecordingVoice, setIsRecordingVoice] = useState(false);
  const [voiceSeconds, setVoiceSeconds] = useState(0);
  const [playingVoiceId, setPlayingVoiceId] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isTyping]);

  // Voice recording timer
  useEffect(() => {
    let timer: NodeJS.Timeout;
    if (isRecordingVoice) {
      timer = setInterval(() => {
        setVoiceSeconds((s) => s + 1);
      }, 1000);
    } else {
      setVoiceSeconds(0);
    }
    return () => clearInterval(timer);
  }, [isRecordingVoice]);

  const handleSend = () => {
    if (!inputText.trim()) return;
    onSendMessage(inputText.trim(), 'text');
    setInputText('');
  };

  const handleFinishVoiceRecord = () => {
    setIsRecordingVoice(false);
    onSendMessage(`Voice message (0:${voiceSeconds < 10 ? '0' : ''}${voiceSeconds})`, 'voice');
  };

  const handleQuickReaction = (emoji: string) => {
    onSendMessage(emoji, 'text');
    setShowEmojiPicker(false);
  };

  return (
    <div className="flex flex-col h-full bg-[#0e1621] relative select-none">
      {/* Top Action Bar */}
      <div className="h-14 bg-[#242f3d] border-b border-[#101419] px-2 flex items-center justify-between z-20 shadow-xs">
        <div className="flex items-center gap-2">
          <button
            onClick={onBack}
            className="p-2 -ml-1 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <TelegramAvatar
            name={chat.title}
            avatarUrl={chat.avatarUrl}
            size={40}
            isOnline={chat.type === 'direct' && chat.title.includes('Pavel')}
          />
          <div className="leading-tight ml-1">
            <h2 className="text-[15px] font-semibold text-white truncate max-w-[180px] sm:max-w-xs">
              {chat.title}
            </h2>
            <p className="text-[12px] text-slate-400">
              {isTyping ? (
                <span className="text-[#4fa0ff] font-medium">typing...</span>
              ) : chat.type === 'group' ? (
                `${chat.memberUids.length || 4} members`
              ) : chat.title.includes('Pavel') ? (
                <span className="text-[#4fa0ff]">online</span>
              ) : (
                'last seen recently'
              )}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1">
          <button
            onClick={() => alert(`Initiating secure encrypted call with ${chat.title}`)}
            className="p-2 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer"
            title="Voice Call"
          >
            <Phone className="w-5 h-5" />
          </button>
          <button
            className="p-2 text-slate-300 hover:text-white hover:bg-slate-700/50 rounded-full cursor-pointer"
            title="Chat Options"
          >
            <MoreVertical className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Telegram Pattern Wallpaper */}
      <div
        className="flex-1 overflow-y-auto px-4 py-3 space-y-2 relative"
        style={{
          backgroundImage: `radial-gradient(circle at 50% 50%, rgba(36, 47, 61, 0.4) 1px, transparent 1px)`,
          backgroundSize: '24px 24px'
        }}
      >
        {/* Date bubble */}
        <div className="flex justify-center my-2">
          <span className="px-3 py-0.5 rounded-full bg-black/40 text-[11px] font-medium text-slate-300 backdrop-blur-xs">
            Today
          </span>
        </div>

        {/* Messages */}
        {messages.map((msg) => {
          const isMe = msg.senderId === 'me' || (currentUser && msg.senderId === currentUser.uid);
          const time = new Date(msg.timestamp).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
          });

          return (
            <div
              key={msg.msgId}
              className={`flex items-end gap-1.5 ${isMe ? 'justify-end' : 'justify-start'}`}
            >
              <div
                className={`relative max-w-[85%] sm:max-w-md px-3.5 py-2 rounded-2xl shadow-sm text-white ${
                  isMe
                    ? 'bg-[#2b5278] rounded-br-[4px]'
                    : 'bg-[#182533] rounded-bl-[4px]'
                }`}
              >
                {/* Voice Note View */}
                {msg.type === 'voice' ? (
                  <div className="flex items-center gap-3 py-1">
                    <button
                      onClick={() =>
                        setPlayingVoiceId(playingVoiceId === msg.msgId ? null : msg.msgId)
                      }
                      className="w-10 h-10 rounded-full bg-[#5288c1] flex items-center justify-center text-white shrink-0 hover:bg-[#4076ad] cursor-pointer"
                    >
                      {playingVoiceId === msg.msgId ? (
                        <Pause className="w-5 h-5 fill-current" />
                      ) : (
                        <Play className="w-5 h-5 fill-current ml-0.5" />
                      )}
                    </button>
                    <div className="flex-1">
                      {/* Fake waveform */}
                      <div className="flex items-center gap-[3px] h-6">
                        {[40, 70, 95, 30, 60, 85, 45, 90, 65, 35, 75, 50, 80, 40].map((h, i) => (
                          <div
                            key={i}
                            className={`w-1 rounded-full transition-all ${
                              playingVoiceId === msg.msgId ? 'bg-white' : 'bg-slate-400'
                            }`}
                            style={{ height: `${h}%` }}
                          />
                        ))}
                      </div>
                      <span className="text-[11px] text-slate-300">
                        {msg.mediaDurationSec ? `0:${msg.mediaDurationSec}` : '0:14'}
                      </span>
                    </div>
                  </div>
                ) : (
                  /* Standard Text */
                  <p className="text-[14.5px] leading-relaxed whitespace-pre-wrap break-words pr-14">
                    {msg.text}
                  </p>
                )}

                {/* Footer Time & Status Ticks */}
                <div className="absolute right-2.5 bottom-1 flex items-center gap-1 text-[11px] text-slate-300/80">
                  <span>{time}</span>
                  {isMe && (
                    <span>
                      {msg.status === 'pending' && <Clock className="w-3 h-3 text-slate-300" />}
                      {msg.status === 'sent' && <Check className="w-3.5 h-3.5 text-[#6ab2f2]" />}
                      {(msg.status === 'delivered' || msg.status === 'read') && (
                        <CheckCheck className="w-3.5 h-3.5 text-[#6ab2f2]" />
                      )}
                    </span>
                  )}
                </div>

                {/* Reactions */}
                {msg.reactions && (
                  <div className="absolute -bottom-2.5 right-4 flex gap-1 bg-[#17212b] border border-slate-700/60 rounded-full px-2 py-0.5 text-[11px] shadow-sm">
                    {Object.entries(msg.reactions).map(([reaction, count]) => (
                      <span key={reaction}>
                        {reaction} {count > 1 ? count : ''}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {/* Typing indicator bubble */}
        {isTyping && (
          <div className="flex items-center gap-2 p-2 rounded-xl bg-[#182533] text-slate-300 text-xs w-fit">
            <span className="w-1.5 h-1.5 rounded-full bg-[#4fa0ff] animate-ping" />
            <span>{chat.title} is typing...</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Attachment Sheet Modal */}
      {showAttachMenu && (
        <div className="absolute bottom-16 inset-x-4 bg-[#242f3d] rounded-2xl p-4 shadow-2xl border border-slate-700/60 z-30 animate-in slide-in-from-bottom duration-150">
          <div className="flex items-center justify-between pb-3 border-b border-slate-700/40 mb-3">
            <span className="text-sm font-semibold text-white">Share Content</span>
            <button
              onClick={() => setShowAttachMenu(false)}
              className="p-1 text-slate-400 hover:text-white cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
          <div className="grid grid-cols-4 gap-3 text-center">
            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('Photo: jetpack_compose_preview.png', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-blue-600/30 text-blue-400 flex items-center justify-center">
                <Image className="w-6 h-6" />
              </div>
              <span className="text-[12px]">Gallery</span>
            </button>

            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('Document: FileDatabaseManager.kt (3.4 KB)', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-emerald-600/30 text-emerald-400 flex items-center justify-center">
                <FileText className="w-6 h-6" />
              </div>
              <span className="text-[12px]">File</span>
            </button>

            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('📍 Shared Location (San Francisco, CA)', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-amber-600/30 text-amber-400 flex items-center justify-center">
                <MapPin className="w-6 h-6" />
              </div>
              <span className="text-[12px]">Location</span>
            </button>

            <button
              onClick={() => {
                setShowAttachMenu(false);
                onSendMessage('📊 Poll: Should we use Gson or kotlinx.serialization for Neont?', 'text');
              }}
              className="flex flex-col items-center gap-1.5 text-slate-200 hover:text-[#5288c1] cursor-pointer"
            >
              <div className="w-12 h-12 rounded-full bg-purple-600/30 text-purple-400 flex items-center justify-center">
                <BarChart2 className="w-6 h-6" />
              </div>
              <span className="text-[12px]">Poll</span>
            </button>
          </div>
        </div>
      )}

      {/* Quick Emoji / Sticker Panel */}
      {showEmojiPicker && (
        <div className="absolute bottom-16 left-4 bg-[#242f3d] rounded-2xl p-3 shadow-2xl border border-slate-700/60 z-30 flex gap-2">
          {['👍', '🔥', '❤️', '👏', '🚀', '⚡', '💯', '✨'].map((emoji) => (
            <button
              key={emoji}
              onClick={() => handleQuickReaction(emoji)}
              className="text-2xl p-1.5 hover:scale-125 transition-transform cursor-pointer"
            >
              {emoji}
            </button>
          ))}
        </div>
      )}

      {/* Composer Bottom Bar */}
      <div className="p-2.5 bg-[#17212b] border-t border-[#101419] flex items-center gap-2">
        {isRecordingVoice ? (
          /* Live Voice Recording UI */
          <div className="flex-1 flex items-center justify-between px-3 py-2 bg-red-950/40 border border-red-800/40 rounded-full animate-pulse">
            <div className="flex items-center gap-2 text-red-400 text-sm font-medium">
              <span className="w-2.5 h-2.5 rounded-full bg-red-500 animate-ping" />
              <span>Recording 0:{voiceSeconds < 10 ? '0' : ''}{voiceSeconds}</span>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setIsRecordingVoice(false)}
                className="text-xs text-slate-400 hover:text-white px-2 py-1 cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleFinishVoiceRecord}
                className="px-3 py-1 bg-red-600 text-white text-xs font-semibold rounded-full hover:bg-red-500 cursor-pointer"
              >
                Send
              </button>
            </div>
          </div>
        ) : (
          /* Standard Composer */
          <>
            <button
              onClick={() => setShowEmojiPicker(!showEmojiPicker)}
              className="p-2 text-slate-400 hover:text-[#5288c1] rounded-full cursor-pointer transition-colors"
            >
              <Smile className="w-6 h-6" />
            </button>

            <input
              type="text"
              placeholder="Message"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handleSend();
                }
              }}
              className="flex-1 bg-transparent text-white text-[15px] placeholder-slate-400 outline-none px-2"
            />

            <button
              onClick={() => setShowAttachMenu(!showAttachMenu)}
              className="p-2 text-slate-400 hover:text-white rounded-full cursor-pointer transition-colors"
            >
              <Paperclip className="w-5 h-5 -rotate-45" />
            </button>

            {inputText.trim() ? (
              <button
                onClick={handleSend}
                className="w-10 h-10 rounded-full bg-[#5288c1] hover:bg-[#4076ad] flex items-center justify-center text-white shadow-md cursor-pointer transition-transform active:scale-95"
              >
                <Send className="w-5 h-5 -ml-0.5" />
              </button>
            ) : (
              <button
                onClick={() => setIsRecordingVoice(true)}
                className="w-10 h-10 rounded-full bg-[#242f3d] hover:bg-[#2e3b4d] text-slate-300 hover:text-[#5288c1] flex items-center justify-center cursor-pointer transition-colors"
                title="Hold or click to record voice note"
              >
                <Mic className="w-5 h-5" />
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
};
