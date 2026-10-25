import React from 'react';

interface TelegramAvatarProps {
  name: string;
  avatarUrl?: string;
  size?: number;
  isOnline?: boolean;
  hasStory?: boolean;
  storyViewed?: boolean;
  className?: string;
}

const AVATAR_GRADIENTS = [
  'from-[#E17076] to-[#E6555C]', // Red
  'from-[#FAA774] to-[#F28C48]', // Orange
  'from-[#A695E7] to-[#8C74DE]', // Violet
  'from-[#7BC862] to-[#59B23E]', // Green
  'from-[#6EC9CB] to-[#4DB7B9]', // Cyan
  'from-[#65AADD] to-[#4092D6]', // Blue
  'from-[#EE7AAE] to-[#E25494]'  // Pink
];

export const TelegramAvatar: React.FC<TelegramAvatarProps> = ({
  name,
  avatarUrl,
  size = 48,
  isOnline = false,
  hasStory = false,
  storyViewed = false,
  className = ''
}) => {
  const initial = name ? name.trim().charAt(0).toUpperCase() : '?';
  const colorIndex = Math.abs(name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)) % AVATAR_GRADIENTS.length;
  const gradient = AVATAR_GRADIENTS[colorIndex];

  return (
    <div className={`relative inline-flex items-center justify-center shrink-0 select-none ${className}`}>
      {/* Story Ring */}
      {hasStory && (
        <div
          className={`absolute inset-0 -m-[2.5px] rounded-full border-2 ${
            storyViewed ? 'border-slate-500/60' : 'border-[#3390ec]'
          } animate-pulse`}
          style={{ width: size + 5, height: size + 5 }}
        />
      )}

      {/* Main Avatar Bubble */}
      <div
        className={`rounded-full overflow-hidden flex items-center justify-center font-semibold text-white shadow-sm ${
          avatarUrl ? 'bg-slate-800' : `bg-gradient-to-b ${gradient}`
        }`}
        style={{ width: size, height: size, fontSize: Math.max(12, Math.floor(size * 0.42)) }}
      >
        {avatarUrl ? (
          <img
            src={avatarUrl}
            alt={name}
            referrerPolicy="no-referrer"
            className="w-full h-full object-cover"
            onError={(e) => {
              // fallback to initials
              (e.currentTarget as HTMLImageElement).style.display = 'none';
            }}
          />
        ) : (
          <span>{initial}</span>
        )}
      </div>

      {/* Online indicator dot */}
      {isOnline && !hasStory && (
        <span
          className="absolute bottom-0 right-0 rounded-full bg-[#4fa0ff] border-2 border-[#17212b]"
          style={{
            width: Math.max(10, Math.floor(size * 0.26)),
            height: Math.max(10, Math.floor(size * 0.26))
          }}
          title="Online"
        />
      )}
    </div>
  );
};
