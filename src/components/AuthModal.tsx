import React, { useState } from 'react';
import { KeyRound, X, LogIn, UserPlus, LogOut, CheckCircle } from 'lucide-react';
import { firebaseSim } from '../services/firebaseSimulator';
import { User } from '../types';

interface AuthModalProps {
  isOpen: boolean;
  currentUser: User | null;
  onClose: () => void;
  onAuthSuccess: (user: User | null) => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({
  isOpen,
  currentUser,
  onClose,
  onAuthSuccess
}) => {
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [username, setUsername] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      if (isRegisterMode) {
        if (!email || !password || !displayName) {
          throw new Error('Please fill all required fields');
        }
        const user = await firebaseSim.registerWithEmail(
          email,
          password,
          displayName,
          username || email.split('@')[0]
        );
        onAuthSuccess(user);
        onClose();
      } else {
        if (!email || !password) {
          throw new Error('Please enter email and password');
        }
        const user = await firebaseSim.loginWithEmail(email, password);
        onAuthSuccess(user);
        onClose();
      }
    } catch (err: any) {
      setError(err.message || 'Authentication error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    firebaseSim.logout();
    onAuthSuccess(null);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm select-none">
      <div className="relative w-full max-w-md bg-[#17212b] rounded-2xl border border-slate-700/60 shadow-2xl p-6 overflow-hidden animate-in zoom-in-95 duration-150">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 text-slate-400 hover:text-white hover:bg-slate-700/50 rounded-lg cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-4">
          <div className="p-2.5 rounded-xl bg-amber-500/20 text-amber-400">
            <KeyRound className="w-6 h-6" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">Firebase Authentication</h2>
            <p className="text-xs text-slate-400">Neont Hybrid Auth & Profile Store</p>
          </div>
        </div>

        {currentUser ? (
          /* Already Logged In View */
          <div className="space-y-4">
            <div className="p-4 rounded-xl bg-[#242f3d] border border-slate-700/50 flex items-start gap-3">
              <CheckCircle className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
              <div>
                <p className="text-sm font-semibold text-white">Currently Signed In</p>
                <p className="text-xs text-slate-300 mt-1">UID: <span className="font-mono text-emerald-400">{currentUser.uid}</span></p>
                <p className="text-xs text-slate-300">Name: <span className="text-white font-medium">{currentUser.firstName} {currentUser.lastName}</span></p>
                <p className="text-xs text-slate-300">Username: <span className="text-[#5288c1]">@{currentUser.username}</span></p>
                <p className="text-xs text-slate-400 mt-1 font-mono">Persisted in: context.filesDir/neont_db/users.json</p>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-medium text-sm cursor-pointer transition-colors"
            >
              <LogOut className="w-4 h-4" />
              <span>Sign Out</span>
            </button>
          </div>
        ) : (
          /* Form for Register / Login */
          <form onSubmit={handleSubmit} className="space-y-3.5">
            {error && (
              <div className="p-2.5 rounded-lg bg-rose-950/60 border border-rose-800 text-rose-300 text-xs">
                {error}
              </div>
            )}

            {isRegisterMode && (
              <>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Display Name</label>
                  <input
                    type="text"
                    required
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    placeholder="e.g. Alex Morgan"
                    className="w-full px-3 py-2 bg-[#242f3d] border border-slate-700 rounded-lg text-white text-sm outline-none focus:border-[#5288c1]"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Username (@handle)</label>
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder="e.g. alexm"
                    className="w-full px-3 py-2 bg-[#242f3d] border border-slate-700 rounded-lg text-white text-sm outline-none focus:border-[#5288c1]"
                  />
                </div>
              </>
            )}

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Email / Phone</label>
              <input
                type="text"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                className="w-full px-3 py-2 bg-[#242f3d] border border-slate-700 rounded-lg text-white text-sm outline-none focus:border-[#5288c1]"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1">Password</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-3 py-2 bg-[#242f3d] border border-slate-700 rounded-lg text-white text-sm outline-none focus:border-[#5288c1]"
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl bg-[#5288c1] hover:bg-[#4076ad] text-white font-medium text-sm cursor-pointer transition-colors disabled:opacity-50"
            >
              {isRegisterMode ? <UserPlus className="w-4 h-4" /> : <LogIn className="w-4 h-4" />}
              <span>{isLoading ? 'Processing...' : isRegisterMode ? 'Create Account' : 'Sign In'}</span>
            </button>

            <div className="pt-2 text-center">
              <button
                type="button"
                onClick={() => {
                  setError(null);
                  setIsRegisterMode(!isRegisterMode);
                }}
                className="text-xs text-[#5288c1] hover:underline cursor-pointer"
              >
                {isRegisterMode
                  ? 'Already have an account? Sign In'
                  : "Don't have an account? Register new user"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
