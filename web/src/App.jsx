import React, { useState, useEffect } from 'react';
import { DEMO_USERS, INITIAL_TENDERS, INITIAL_BIDS } from './data/mockData';
import LoginScreen from './components/LoginScreen';
import Navbar from './components/Navbar';
import Dashboard from './components/Dashboard';
import AITenderDesigner from './components/AITenderDesigner';
import BidSubmissionPortal from './components/BidSubmissionPortal';
import EvaluationRoom from './components/EvaluationRoom';

export default function App() {
  // Session & persistence state
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = localStorage.getItem('tenderx_user');
    return saved ? JSON.parse(saved) : null;
  });

  const [tenders, setTenders] = useState(() => {
    const saved = localStorage.getItem('tenderx_tenders');
    return saved ? JSON.parse(saved) : INITIAL_TENDERS;
  });

  const [bids, setBids] = useState(() => {
    const saved = localStorage.getItem('tenderx_bids');
    return saved ? JSON.parse(saved) : INITIAL_BIDS;
  });

  const [activeTab, setActiveTab] = useState('dashboard');
  const [targetTenderId, setTargetTenderId] = useState(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const roleParam = params.get('role');
    const tabParam = params.get('tab');
    if (roleParam && DEMO_USERS[roleParam]) {
      setCurrentUser(DEMO_USERS[roleParam]);
    }
    if (tabParam) {
      setActiveTab(tabParam);
    }
  }, []);

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem('tenderx_tenders', JSON.stringify(tenders));
  }, [tenders]);

  useEffect(() => {
    localStorage.setItem('tenderx_bids', JSON.stringify(bids));
  }, [bids]);

  useEffect(() => {
    if (currentUser) {
      localStorage.setItem('tenderx_user', JSON.stringify(currentUser));
    } else {
      localStorage.removeItem('tenderx_user');
    }
  }, [currentUser]);

  const handleLogin = (user) => {
    setCurrentUser(user);
    setActiveTab('dashboard');
  };

  const handleLogout = () => {
    setCurrentUser(null);
  };

  const handlePublishTender = (newTender) => {
    setTenders(prev => [newTender, ...prev]);
    setActiveTab('dashboard');
  };

  const handleSubmitBid = (newBid) => {
    setBids(prev => [newBid, ...prev]);
    setActiveTab('eval');
    setTargetTenderId(newBid.tenderId);
  };

  const handleAwardContract = (tenderId, awardedBidId, awardedBidderName) => {
    setTenders(prev => prev.map(t => {
      if (t.id === tenderId) {
        return {
          ...t,
          status: 'AWARDED',
          awardedBidId,
          awardedBidderName
        };
      }
      return t;
    }));
  };

  const handleSelectTenderForBid = (tenderId) => {
    setTargetTenderId(tenderId);
    setActiveTab('bids');
  };

  const handleSelectTenderForEval = (tenderId) => {
    setTargetTenderId(tenderId);
    setActiveTab('eval');
  };

  // If not logged in, show dedicated 3-Role Login Screen
  if (!currentUser) {
    return <LoginScreen onLogin={handleLogin} />;
  }

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col text-slate-100">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      <main className="flex-1 pb-16">
        {activeTab === 'dashboard' && (
          <Dashboard
            tenders={tenders}
            bids={bids}
            currentUser={currentUser}
            onNavigate={setActiveTab}
            onSelectTenderForBid={handleSelectTenderForBid}
            onSelectTenderForEval={handleSelectTenderForEval}
          />
        )}

        {activeTab === 'designer' && (
          <AITenderDesigner
            onPublishTender={handlePublishTender}
            onCancel={() => setActiveTab('dashboard')}
          />
        )}

        {activeTab === 'bids' && (
          <BidSubmissionPortal
            tenders={tenders}
            selectedTenderId={targetTenderId}
            onSubmitBid={handleSubmitBid}
            onCancel={() => setActiveTab('dashboard')}
            currentUser={currentUser}
          />
        )}

        {activeTab === 'eval' && (
          <EvaluationRoom
            tenders={tenders}
            bids={bids}
            selectedTenderId={targetTenderId}
            onAwardContract={handleAwardContract}
            onCancel={() => setActiveTab('dashboard')}
            currentUser={currentUser}
          />
        )}
      </main>

      {/* Modern footer */}
      <footer className="border-t border-slate-900 py-6 text-center text-xs text-slate-500">
        <p>TenderX Platform &bull; Human-in-the-Loop LLM Criteria Generation + Deterministic Procurement Engine</p>
      </footer>
    </div>
  );
}
