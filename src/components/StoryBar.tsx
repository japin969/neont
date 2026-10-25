import React, { useState } from 'react';
import { Plus, X } from 'lucide-react';
import { Story } from '../types';
import { TelegramAvatar } from './TelegramAvatar';

interface StoryBarProps {
  stories: Story[];
  onAddStory?: () => void;
}

export const StoryBar: React.FC<StoryBarProps> = ({ stories, onAddStory }) => {
  const [activeStory, setActiveStory] = useState<Story | null>(null);

  return (
    <>
      <div className="flex items-center gap-3 px-4 py-2.5 overflow-x-auto no-scrollbar border-b border-[#101419] bg-[#17212b]/80 backdrop-blur-sm">
        {/* Add My Story */}
        <button
          onClick={onAddStory}
          className="flex flex-col items-center gap-1.5 shrink-0 focus:outline-none group cursor-pointer"
        >
          <div className="relative w-12 h-12 rounded-full bg-slate-800/90 border border-slate-700/60 flex items-center justify-center text-slate-300 group-hover:border-[#5288c1] transition-colors">
            <Plus className="w-5 h-5 text-[#5288c1]" />
            <span className="absolute bottom-0 right-0 w-4 h-4 rounded-full bg-[#5288c1] border border-[#17212b] flex items-center justify-center text-[10px] text-white font-bold">
              +
            </span>
          </div>
          <span className="text-[11px] text-slate-400 group-hover:text-slate-200 truncate max-w-[60px]">
            My Story
          </span>
        </button>

        {/* Contact stories */}
        {stories.map((story) => (
          <button
            key={story.storyId}
            onClick={() => setActiveStory(story)}
            className="flex flex-col items-center gap-1.5 shrink-0 focus:outline-none group cursor-pointer"
          >
            <TelegramAvatar
              name={story.userName}
              avatarUrl={story.userAvatar}
              size={48}
              hasStory={true}
              storyViewed={story.isViewed}
            />
            <span className="text-[11px] text-slate-300 group-hover:text-[#5288c1] truncate max-w-[64px]">
              {story.userName.split(' ')[0]}
            </span>
          </button>
        ))}
      </div>

      {/* Story Viewer Modal */}
      {activeStory && (
        <div className="fixed inset-0 z-50 bg-black/95 flex items-center justify-center p-4">
          <div className="relative w-full max-w-sm rounded-2xl overflow-hidden bg-slate-900 border border-slate-800 shadow-2xl flex flex-col aspect-[9/16]">
            {/* Story progress bar */}
            <div className="absolute top-3 inset-x-3 z-20 flex gap-1">
              <div className="h-1 flex-1 bg-white/40 rounded-full overflow-hidden">
                <div className="h-full bg-white animate-[pulse_4s_linear]" />
              </div>
            </div>

            {/* Header */}
            <div className="absolute top-6 inset-x-4 z-20 flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <TelegramAvatar name={activeStory.userName} avatarUrl={activeStory.userAvatar} size={36} />
                <div>
                  <p className="text-sm font-semibold text-white drop-shadow">{activeStory.userName}</p>
                  <p className="text-[11px] text-slate-300 drop-shadow">2 hours ago</p>
                </div>
              </div>
              <button
                onClick={() => setActiveStory(null)}
                className="p-1.5 rounded-full bg-black/40 text-white hover:bg-black/70 cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Media Image */}
            <img
              src={activeStory.mediaUrl}
              alt="Story"
              referrerPolicy="no-referrer"
              className="w-full h-full object-cover"
            />

            {/* Caption */}
            {activeStory.caption && (
              <div className="absolute bottom-6 inset-x-4 z-20 p-3 rounded-xl bg-black/60 backdrop-blur-md text-white text-sm">
                {activeStory.caption}
              </div>
            )}
          </div>
        </div>
      )}
    </>
  );
};
