import React from 'react';
import { LayoutDashboard, Sparkles, Send, Scale, LogOut, Shield, Building2, HardHat } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, currentUser, onLogout }) {
  const isPublisher = currentUser?.role === 'publisher';
  const isBidder = currentUser?.role === 'bidder';
  const isAdmin = currentUser?.role === 'admin';

  const roleBadgeConfig = {
    publisher: {
      label: 'Issuer Mode',
      color: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30',
      icon: Building2
    },
    bidder: {
      label: 'Contractor Mode',
      color: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
      icon: HardHat
    },
    admin: {
      label: 'Auditor Mode',
      color: 'bg-amber-500/10 text-amber-400 border-amber-500/30',
      icon: Shield
    }
  }[currentUser?.role || 'publisher'];

  const RoleIcon = roleBadgeConfig.icon;

  return (
    <header className="bg-slate-900/90 backdrop-blur-md border-b border-slate-800 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          
          {/* Logo & Role Mode Pill */}
          <div className="flex items-center gap-4">
            <div 
              onClick={() => setActiveTab('dashboard')} 
              className="flex items-center gap-2.5 cursor-pointer group"
            >
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-sky-500 flex items-center justify-center shadow-lg shadow-indigo-600/20 group-hover:scale-105 transition-transform">
                <span className="text-white font-black text-lg">⚡</span>
              </div>
              <div>
                <span className="text-lg font-black tracking-tight text-white flex items-center gap-1.5">
                  TenderX
                </span>
                <span className="block text-[10px] uppercase font-bold tracking-wider text-slate-500">
                  AI Procurement
                </span>
              </div>
            </div>

            <div className={`hidden sm:inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs font-semibold ${roleBadgeConfig.color}`}>
              <RoleIcon className="w-3.5 h-3.5" />
              <span>{roleBadgeConfig.label}</span>
            </div>
          </div>

          {/* Navigation links */}
          <nav className="flex items-center space-x-1 sm:space-x-2">
            <button
              onClick={() => setActiveTab('dashboard')}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all ${
                activeTab === 'dashboard'
                  ? 'bg-slate-800 text-white shadow-sm'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Dashboard</span>
            </button>

            {/* Publisher-exclusive or general */}
            {(isPublisher || isAdmin) && (
              <button
                onClick={() => setActiveTab('designer')}
                className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all ${
                  activeTab === 'designer'
                    ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                }`}
              >
                <Sparkles className="w-4 h-4 text-sky-400" />
                <span>AI Designer</span>
              </button>
            )}

            {/* Bidder or general */}
            {(isBidder || isPublisher || isAdmin) && (
              <button
                onClick={() => setActiveTab('bids')}
                className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all ${
                  activeTab === 'bids'
                    ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                }`}
              >
                <Send className="w-4 h-4" />
                <span>Submit Bid</span>
              </button>
            )}

            {/* Evaluation Room */}
            <button
              onClick={() => setActiveTab('eval')}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-xs sm:text-sm font-semibold transition-all ${
                activeTab === 'eval'
                  ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/20'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <Scale className="w-4 h-4" />
              <span>Evaluation Room</span>
            </button>
          </nav>

          {/* User profile & Logout */}
          <div className="flex items-center gap-3">
            <div className="hidden md:block text-right">
              <div className="text-xs font-bold text-white truncate max-w-[150px]">{currentUser?.name}</div>
              <div className="text-[10px] text-slate-400 truncate max-w-[160px]">{currentUser?.organization}</div>
            </div>

            <button
              onClick={onLogout}
              title="Switch Role / Log Out"
              className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 border border-slate-800 hover:border-rose-500/30 transition-all cursor-pointer flex items-center gap-1.5 text-xs font-semibold"
            >
              <LogOut className="w-4 h-4" />
              <span className="hidden sm:inline">Switch Role</span>
            </button>
          </div>

        </div>
      </div>
    </header>
  );
}

