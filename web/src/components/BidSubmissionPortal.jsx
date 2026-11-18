import React, { useState, useEffect } from 'react';
import { Send, Sparkles, CheckCircle2, ArrowLeft, Building2, IndianRupee, Clock, Check } from 'lucide-react';

export default function BidSubmissionPortal({ tenders, selectedTenderId, onSubmitBid, onCancel, currentUser }) {
  const [activeTenderId, setActiveTenderId] = useState(selectedTenderId || tenders[0]?.id || '');
  const [contractorName, setContractorName] = useState(currentUser?.organization || 'Apex Infrastructure & Geotech Ltd');
  const [totalQuote, setTotalQuote] = useState('2420000');
  const [proposedDays, setProposedDays] = useState('52');
  const [formResponses, setFormResponses] = useState({});
  const [submittedReceipt, setSubmittedReceipt] = useState(null);

  const activeTender = tenders.find(t => t.id === activeTenderId) || tenders[0];

  useEffect(() => {
    if (selectedTenderId) {
      setActiveTenderId(selectedTenderId);
    }
  }, [selectedTenderId]);

  useEffect(() => {
    if (currentUser?.organization) {
      setContractorName(currentUser.organization);
    } else if (currentUser?.name) {
      setContractorName(currentUser.name);
    }
  }, [currentUser]);

  const handleClearForm = () => {
    setFormResponses({});
    setTotalQuote('');
    setProposedDays('');
  };

  // Handle dynamic form change
  const handleFieldChange = (fieldKey, value) => {
    setFormResponses(prev => ({ ...prev, [fieldKey]: value }));
  };

  // Auto-fill button for fast video demo
  const handleAutoFill = () => {
    const auto = {};
    if (activeTender) {
      activeTender.criteria?.forEach(c => {
        c.fields?.forEach(f => {
          const k = f.key.toLowerCase();
          if (f.type === 'ENUM' && f.options?.length) {
            auto[f.key] = f.options[0];
          } else if (f.type === 'NUMBER') {
            if (k.includes('exp') || k.includes('project')) auto[f.key] = '8';
            else if (k.includes('day') || k.includes('time')) auto[f.key] = '50';
            else if (k.includes('cost') || k.includes('quote') || k.includes('amount')) auto[f.key] = '2390000';
            else auto[f.key] = '100';
          } else if (f.type === 'BOOLEAN') {
            auto[f.key] = 'Yes';
          } else {
            if (k.includes('drain')) {
              auto[f.key] = 'Prefabricated RCC slotted U-drains with graded slope filter beds and non-clogging silt pits.';
            } else if (k.includes('compaction') || k.includes('subgrade')) {
              auto[f.key] = 'Subgrade stabilization with 4% cementitious binder compacted to 98% modified Proctor density.';
            } else if (k.includes('safety') || k.includes('traffic')) {
              auto[f.key] = 'Dedicated two-lane daytime diversion with computerized flashing beacons and certified marshals.';
            } else {
              auto[f.key] = 'Strict QA/QC protocol with weekly core extraction and NABL testing.';
            }
          }
        });
      });
    }
    setFormResponses(auto);
    setTotalQuote('2390000');
    setProposedDays('50');
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!activeTender) return;

    const bid = {
      id: 'BID-' + Math.floor(100 + Math.random() * 900),
      tenderId: activeTender.id,
      bidderId: currentUser?.id || 'usr_contractor',
      bidderName: contractorName.trim() || 'Apex Infrastructure & Geotech Ltd',
      totalQuote: Number(totalQuote),
      proposedDays: Number(proposedDays),
      submittedAt: new Date().toISOString().replace('T', ' ').substring(0, 16),
      status: 'SUBMITTED',
      responses: formResponses
    };

    onSubmitBid(bid);
    setSubmittedReceipt(bid);
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      
      {/* Header */}
      <div>
        <button
          onClick={onCancel}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-400 hover:text-white mb-2 cursor-pointer"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          Back to Dashboard
        </button>
        <h2 className="text-2xl font-black text-white tracking-tight flex items-center gap-2">
          <Send className="w-6 h-6 text-emerald-400" />
          Bidder Submission Portal (Dynamic Form Engine)
        </h2>
        <p className="text-xs text-slate-400 mt-1">
          Step 5: Dynamic form rendered directly from the AI-generated and issuer-approved schema.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        
        {/* Tender Selection & Contractor Basics Card */}
        <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-4">
          <div className="flex items-center justify-between">
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-300">
              Select Target Published Tender *
            </label>
            <span className="text-[11px] text-emerald-400 font-semibold font-mono">
              Live Schema Connected
            </span>
          </div>

          <select
            value={activeTenderId}
            onChange={(e) => setActiveTenderId(e.target.value)}
            className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-transparent transition-all"
          >
            {tenders.map(t => (
              <option key={t.id} value={t.id}>
                [{t.id}] {t.title} (Cap: ₹{t.estimatedBudget?.toLocaleString()})
              </option>
            ))}
          </select>

          {activeTender && (
            <div className="p-3 rounded-xl bg-slate-950 border border-slate-800/80 text-xs text-slate-400 flex items-center justify-between">
              <div>
                <span className="font-bold text-white">Estimated Budget:</span> ₹{activeTender.estimatedBudget?.toLocaleString()}
              </div>
              <div>
                <span className="font-bold text-white">Max Timeline:</span> {activeTender.maxTimelineDays} Days
              </div>
              <div>
                <span className="font-bold text-white">Criteria Count:</span> {activeTender.criteria?.length || 5} Criteria
              </div>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1.5">
                Contractor Enterprise Name *
              </label>
              <input
                type="text"
                value={contractorName}
                onChange={(e) => setContractorName(e.target.value)}
                required
                className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1.5">
                Total Commercial Quote (₹) *
              </label>
              <input
                type="number"
                value={totalQuote}
                onChange={(e) => setTotalQuote(e.target.value)}
                required
                className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1.5">
                Proposed Timeline (Days) *
              </label>
              <input
                type="number"
                value={proposedDays}
                onChange={(e) => setProposedDays(e.target.value)}
                required
                className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>
          </div>
        </div>

        {/* Dynamic Schema Fields Section */}
        {activeTender && (
          <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-2 border-b border-slate-800">
              <div>
                <h3 className="text-base font-bold text-white">Dynamic Form Requirements</h3>
                <p className="text-xs text-slate-400">Rendered dynamically from the approved AI specification</p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={handleClearForm}
                  className="px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-400 bg-slate-800 hover:bg-slate-700 hover:text-white border border-slate-700 transition-all cursor-pointer"
                >
                  Clear Fields
                </button>

                <button
                  type="button"
                  onClick={handleAutoFill}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-bold text-emerald-300 bg-emerald-500/15 border border-emerald-500/30 hover:bg-emerald-500/25 transition-all cursor-pointer"
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  <span>🪄 Auto-Fill Proposal (For Demo)</span>
                </button>
              </div>
            </div>

            <div className="space-y-6">
              {activeTender.criteria?.map((crit, cIdx) => (
                <div key={crit.key || cIdx} className="space-y-3">
                  <div className="flex items-center gap-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-500/15 text-indigo-400 border border-indigo-500/30">
                      Criterion {cIdx + 1} &bull; {crit.weight}%
                    </span>
                    <span className="text-xs font-bold text-slate-200">{crit.name}</span>
                  </div>

                  <div className="space-y-3 pl-2 sm:pl-4 border-l border-slate-800">
                    {crit.fields?.map(field => {
                      const val = formResponses[field.key] || '';

                      return (
                        <div key={field.id} className="space-y-1">
                          <label className="block text-xs font-semibold text-slate-300">
                            {field.label} {field.required && <span className="text-rose-400">*</span>}
                          </label>

                          {field.type === 'ENUM' && field.options?.length ? (
                            <select
                              value={val || field.options[0]}
                              onChange={(e) => handleFieldChange(field.key, e.target.value)}
                              className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-white focus:outline-none focus:ring-1 focus:ring-emerald-500"
                            >
                              {field.options.map((opt, i) => (
                                <option key={i} value={opt}>{opt}</option>
                              ))}
                            </select>
                          ) : field.type === 'NUMBER' ? (
                            <input
                              type="number"
                              value={val}
                              onChange={(e) => handleFieldChange(field.key, e.target.value)}
                              placeholder={`Enter number ${field.unit ? `(${field.unit})` : ''}`}
                              className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                            />
                          ) : field.type === 'BOOLEAN' ? (
                            <label className="flex items-center gap-2 text-xs text-slate-300 cursor-pointer pt-1">
                              <input
                                type="checkbox"
                                checked={val === 'Yes'}
                                onChange={(e) => handleFieldChange(field.key, e.target.checked ? 'Yes' : 'No')}
                                className="w-4 h-4 rounded bg-slate-950 border-slate-700 text-emerald-500 focus:ring-emerald-500"
                              />
                              <span>Yes, compliant with specification</span>
                            </label>
                          ) : (
                            <textarea
                              rows={2}
                              value={val}
                              onChange={(e) => handleFieldChange(field.key, e.target.value)}
                              placeholder={field.placeholder || "Describe your approach..."}
                              className="w-full px-3.5 py-2 rounded-lg bg-slate-950 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
                            />
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-end">
              <button
                type="submit"
                className="flex items-center gap-2 px-6 py-3 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-600/25 transition-all cursor-pointer"
              >
                <Send className="w-4 h-4" />
                <span>Submit Official Sealed Bid</span>
              </button>
            </div>
          </div>
        )}

      </form>

      {/* Submission Receipt Modal */}
      {submittedReceipt && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-emerald-500/40 rounded-2xl p-6 sm:p-8 max-w-md w-full text-center space-y-4 shadow-2xl">
            <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 mx-auto flex items-center justify-center">
              <Check className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold text-white">Bid Submitted & Sealed!</h3>
            <div className="p-3 bg-slate-950 rounded-xl text-xs text-slate-300 font-mono space-y-1 text-left">
              <div>Receipt ID: <span className="text-emerald-400 font-bold">{submittedReceipt.id}</span></div>
              <div>Contractor: <span className="text-white">{submittedReceipt.bidderName}</span></div>
              <div>Quoted Amount: <span className="text-white font-bold">₹{submittedReceipt.totalQuote?.toLocaleString()}</span></div>
              <div>Timeline: <span className="text-white">{submittedReceipt.proposedDays} Days</span></div>
            </div>
            <p className="text-xs text-slate-400">
              Your responses have been sealed into the database. Open the Evaluation Room to execute the deterministic scoring engine.
            </p>
            <button
              onClick={onCancel}
              className="w-full py-2.5 rounded-xl font-bold text-sm text-white bg-emerald-600 hover:bg-emerald-500 transition-all cursor-pointer"
            >
              Back to Dashboard
            </button>
          </div>
        </div>
      )}

    </div>
  );
}

