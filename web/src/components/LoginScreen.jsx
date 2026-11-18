import React, { useState } from 'react';
import { DEMO_USERS } from '../data/mockData';
import { Shield, Sparkles, Building2, HardHat, FileCheck2, ArrowRight, Lock, Mail, Eye, EyeOff, UserPlus, LogIn, UserCheck } from 'lucide-react';

export default function LoginScreen({ onLogin }) {
  const [mode, setMode] = useState('signin'); // 'signin' | 'register'
  const [selectedPresetKey, setSelectedPresetKey] = useState('publisher');

  // Sign in form state
  const [email, setEmail] = useState(DEMO_USERS.publisher.email);
  const [password, setPassword] = useState(DEMO_USERS.publisher.password);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');

  // Register form state
  const [regName, setRegName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regRole, setRegRole] = useState('bidder');
  const [regOrg, setRegOrg] = useState('');
  const [regSuccess, setRegSuccess] = useState('');

  // 5 quick demo accounts: 1 Publisher, 3 distinct Bidders, 1 Auditor
  const quickAccounts = [
    { key: 'publisher', label: 'Publisher (NHAI)', icon: Building2, user: DEMO_USERS.publisher, color: 'indigo' },
    { key: 'bidder1', label: 'Bidder 1 (Apex Infra)', icon: HardHat, user: DEMO_USERS.bidder1, color: 'emerald' },
    { key: 'bidder2', label: 'Bidder 2 (BuildWell)', icon: HardHat, user: DEMO_USERS.bidder2, color: 'teal' },
    { key: 'bidder3', label: 'Bidder 3 (Sterling)', icon: HardHat, user: DEMO_USERS.bidder3, color: 'cyan' },
    { key: 'admin', label: 'Auditor (Vigilance)', icon: Shield, user: DEMO_USERS.admin, color: 'amber' }
  ];

  const handleSelectPreset = (acc) => {
    setSelectedPresetKey(acc.key);
    setEmail(acc.user.email);
    setPassword(acc.user.password);
    setError('');
  };

  const handleSignIn = (e) => {
    e.preventDefault();
    setError('');

    // Check predefined users
    for (const key of Object.keys(DEMO_USERS)) {
      const u = DEMO_USERS[key];
      if (u.email.toLowerCase() === email.trim().toLowerCase() && u.password === password) {
        onLogin(u);
        return;
      }
    }

    // Check custom registered users from localStorage
    const savedCustomUsers = JSON.parse(localStorage.getItem('tenderx_registered_users') || '[]');
    const foundCustom = savedCustomUsers.find(
      u => u.email.toLowerCase() === email.trim().toLowerCase() && u.password === password
    );

    if (foundCustom) {
      onLogin(foundCustom);
      return;
    }

    setError('Invalid email or password. Use one-click demo presets or register a new identity.');
  };

  const handleRegister = (e) => {
    e.preventDefault();
    setError('');

    if (!regName.trim() || !regEmail.trim() || !regPassword.trim() || !regOrg.trim()) {
      setError('Please fill in all registration fields.');
      return;
    }

    const newUser = {
      id: `usr_${Date.now()}`,
      email: regEmail.trim().toLowerCase(),
      password: regPassword,
      name: regName.trim(),
      role: regRole,
      roleTitle: regRole === 'bidder' ? 'Registered Contractor / Bidder' : (regRole === 'publisher' ? 'Tender Authority / Issuer' : 'Procurement Auditor'),
      organization: regOrg.trim(),
      badgeColor: regRole === 'bidder' ? 'emerald' : (regRole === 'publisher' ? 'indigo' : 'amber'),
      isCustomUser: true
    };

    // Save to localStorage
    const existing = JSON.parse(localStorage.getItem('tenderx_registered_users') || '[]');
    existing.push(newUser);
    localStorage.setItem('tenderx_registered_users', JSON.stringify(existing));

    setRegSuccess(`Account for ${newUser.name} registered! Logging in...`);
    setTimeout(() => {
      onLogin(newUser);
    }, 600);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-10 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Background ambient lighting */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-emerald-600/10 rounded-full blur-3xl pointer-events-none" />

      {/* Brand Header */}
      <div className="sm:mx-auto sm:w-full sm:max-w-xl z-10 text-center px-4">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold mb-3">
          <Sparkles className="w-3.5 h-3.5 text-indigo-400" />
          LLM-Assisted Tender Evaluation Platform
        </div>
        <h1 className="text-4xl font-black text-white tracking-tight flex items-center justify-center gap-2">
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-sky-400 via-indigo-400 to-emerald-400">
            TenderX
          </span>
        </h1>
        <p className="mt-1 text-xs sm:text-sm text-slate-400">
          Deterministic Java Procurement Engine with Live LLM Criteria Generation
        </p>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-2xl z-10 px-4">
        <div className="bg-slate-900/90 backdrop-blur-xl border border-slate-800 rounded-2xl shadow-2xl p-6 sm:p-8 space-y-6">
          
          {/* Mode Tabs: Sign In vs Register */}
          <div className="flex border-b border-slate-800 pb-3 justify-between items-center">
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setMode('signin')}
                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all cursor-pointer ${
                  mode === 'signin'
                    ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-600/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <LogIn className="w-4 h-4" />
                <span>Preconfigured & Sign In</span>
              </button>

              <button
                type="button"
                onClick={() => setMode('register')}
                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all cursor-pointer ${
                  mode === 'register'
                    ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/20'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <UserPlus className="w-4 h-4" />
                <span>Register Custom ID (Make Own)</span>
              </button>
            </div>

            <span className="text-[11px] font-mono text-slate-500 hidden sm:inline">
              Multi-Role Auth
            </span>
          </div>

          {mode === 'signin' ? (
            <>
              {/* Preconfigured One-Click Presets */}
              <div>
                <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-2.5">
                  Select Preconfigured Demo Identity (1-Click Fill):
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-5 gap-2">
                  {quickAccounts.map((acc) => {
                    const Icon = acc.icon;
                    const isSelected = selectedPresetKey === acc.key;

                    return (
                      <button
                        key={acc.key}
                        type="button"
                        onClick={() => handleSelectPreset(acc)}
                        className={`flex flex-col items-center gap-1.5 p-2.5 rounded-xl border text-center transition-all cursor-pointer ${
                          isSelected
                            ? 'bg-indigo-600/20 border-indigo-500 text-white shadow-md'
                            : 'bg-slate-950/70 border-slate-800 text-slate-400 hover:border-slate-700 hover:text-slate-200'
                        }`}
                      >
                        <Icon className={`w-4 h-4 ${isSelected ? 'text-indigo-400' : 'text-slate-500'}`} />
                        <span className="text-[11px] font-bold leading-tight truncate w-full">{acc.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* Active preset details banner */}
              <div className="p-3 rounded-xl bg-slate-950/80 border border-slate-800 flex items-center justify-between text-xs">
                <div>
                  <div className="font-bold text-white flex items-center gap-2">
                    <UserCheck className="w-3.5 h-3.5 text-emerald-400" />
                    <span>{DEMO_USERS[selectedPresetKey]?.name || 'Custom Account'}</span>
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    {DEMO_USERS[selectedPresetKey]?.roleTitle} &bull; {DEMO_USERS[selectedPresetKey]?.organization}
                  </div>
                </div>
                <span className="text-[10px] px-2 py-0.5 rounded font-mono font-bold bg-slate-800 text-slate-300">
                  {DEMO_USERS[selectedPresetKey]?.role?.toUpperCase()}
                </span>
              </div>

              <form onSubmit={handleSignIn} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Email / User ID</label>
                  <div className="relative rounded-lg shadow-sm">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Mail className="h-4 w-4" />
                    </div>
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      className="block w-full pl-9 pr-3 py-2.5 bg-slate-950 border border-slate-800 rounded-lg text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Password</label>
                  <div className="relative rounded-lg shadow-sm">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
                      <Lock className="h-4 w-4" />
                    </div>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      className="block w-full pl-9 pr-10 py-2.5 bg-slate-950 border border-slate-800 rounded-lg text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center text-slate-500 hover:text-slate-300 cursor-pointer"
                    >
                      {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                {error && (
                  <div className="text-rose-400 text-xs font-medium bg-rose-500/10 border border-rose-500/20 rounded-lg p-2.5 text-center">
                    {error}
                  </div>
                )}

                <button
                  type="submit"
                  className="w-full py-3 px-4 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-indigo-600 to-sky-600 hover:from-indigo-500 hover:to-sky-500 shadow-lg shadow-indigo-600/25 transition-all cursor-pointer flex items-center justify-center gap-2"
                >
                  <span>Sign In to TenderX</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </form>
            </>
          ) : (
            /* Registration Form: Make Own ID */
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="text-xs text-slate-400">
                Create a custom contractor bidder identity, tender publisher, or auditor with your own custom credentials.
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Full Representative Name *</label>
                  <input
                    type="text"
                    value={regName}
                    onChange={(e) => setRegName(e.target.value)}
                    placeholder="e.g. Rahul Sharma"
                    required
                    className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Role / Account Type *</label>
                  <select
                    value={regRole}
                    onChange={(e) => setRegRole(e.target.value)}
                    className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  >
                    <option value="bidder">Bidder (Contractor / Vendor)</option>
                    <option value="publisher">Publisher (Tender Authority / Issuer)</option>
                    <option value="admin">Auditor (Vigilance & Compliance)</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Organization / Enterprise Name *</label>
                  <input
                    type="text"
                    value={regOrg}
                    onChange={(e) => setRegOrg(e.target.value)}
                    placeholder="e.g. Sharma Civil Infra & Roadworks"
                    required
                    className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Email / Login ID *</label>
                  <input
                    type="email"
                    value={regEmail}
                    onChange={(e) => setRegEmail(e.target.value)}
                    placeholder="e.g. rahul@sharmainfra.com"
                    required
                    className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Password *</label>
                <input
                  type="password"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  placeholder="Create password"
                  required
                  className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              {error && (
                <div className="text-rose-400 text-xs font-medium bg-rose-500/10 border border-rose-500/20 rounded-lg p-2.5 text-center">
                  {error}
                </div>
              )}

              {regSuccess && (
                <div className="text-emerald-400 text-xs font-medium bg-emerald-500/10 border border-emerald-500/20 rounded-lg p-2.5 text-center">
                  {regSuccess}
                </div>
              )}

              <button
                type="submit"
                className="w-full py-3 px-4 rounded-xl text-sm font-bold text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-600/25 transition-all cursor-pointer flex items-center justify-center gap-2"
              >
                <UserPlus className="w-4 h-4" />
                <span>Register Identity & Sign In</span>
              </button>
            </form>
          )}

          <div className="pt-4 border-t border-slate-800/80 text-center">
            <p className="text-[11px] text-slate-500 flex items-center justify-center gap-1.5">
              <FileCheck2 className="w-3.5 h-3.5 text-emerald-400" />
              Connected to 3 Preconfigured Bidders + Custom Registration + Gemini / Grok LLM Engine
            </p>
          </div>

        </div>
      </div>
    </div>
  );
}
