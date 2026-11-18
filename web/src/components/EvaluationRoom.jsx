import React, { useState, useEffect } from 'react';
import { evaluateTenderBids } from '../services/scoringEngine';
import confetti from 'canvas-confetti';
import { Scale, Trophy, CheckCircle2, ArrowLeft, ArrowUpRight, IndianRupee, ShieldCheck, Sparkles, AlertTriangle } from 'lucide-react';

export default function EvaluationRoom({ tenders, bids, selectedTenderId, onAwardContract, onCancel, currentUser }) {
  const [activeTenderId, setActiveTenderId] = useState(selectedTenderId || tenders[0]?.id || '');
  const [evaluationResults, setEvaluationResults] = useState([]);
  const [hasEvaluated, setHasEvaluated] = useState(false);
  const [awardedSuccess, setAwardedSuccess] = useState(false);

  const activeTender = tenders.find(t => t.id === activeTenderId) || tenders[0];
  const tenderBids = bids.filter(b => b.tenderId === activeTender?.id);

  useEffect(() => {
    if (selectedTenderId) {
      setActiveTenderId(selectedTenderId);
    }
  }, [selectedTenderId]);

  useEffect(() => {
    // Automatically run evaluation on tender change
    if (activeTender && tenderBids.length > 0) {
      const results = evaluateTenderBids(activeTender, tenderBids);
      setEvaluationResults(results);
      setHasEvaluated(true);
    } else {
      setEvaluationResults([]);
      setHasEvaluated(false);
    }
  }, [activeTenderId, tenders, bids]);

  const handleRunEvaluation = () => {
    if (!activeTender || tenderBids.length === 0) return;
    const results = evaluateTenderBids(activeTender, tenderBids);
    setEvaluationResults(results);
    setHasEvaluated(true);
  };

  const handleAward = (winner) => {
    onAwardContract(activeTender.id, winner.bidId, winner.bidderName);
    setAwardedSuccess(true);
    confetti({
      particleCount: 90,
      spread: 70,
      origin: { y: 0.6 }
    });
  };

  const winner = evaluationResults[0];

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
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
          <Scale className="w-6 h-6 text-indigo-400" />
          Deterministic Evaluation & Awarding Room
        </h2>
        <p className="text-xs text-slate-400 mt-1">
          Step 6: Mathematical, 100% reproducible scoring engine applying approved criteria weights and inverse-cost formulas.
        </p>
      </div>

      {/* Tender Selection Bar */}
      <div className="p-5 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex-1">
          <label className="block text-xs font-bold uppercase tracking-wider text-slate-400 mb-1.5">
            Select Tender to Evaluate:
          </label>
          <select
            value={activeTenderId}
            onChange={(e) => setActiveTenderId(e.target.value)}
            className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            {tenders.map(t => (
              <option key={t.id} value={t.id}>
                [{t.id}] {t.title} ({t.status})
              </option>
            ))}
          </select>
        </div>

        <div className="self-end sm:self-center">
          <button
            onClick={handleRunEvaluation}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-xs sm:text-sm text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-600/20 transition-all cursor-pointer"
          >
            <Scale className="w-4 h-4" />
            <span>Re-Run Deterministic Evaluation</span>
          </button>
        </div>
      </div>

      {tenderBids.length === 0 ? (
        <div className="p-12 text-center rounded-2xl bg-slate-900 border border-slate-800 space-y-3">
          <AlertTriangle className="w-8 h-8 text-amber-400 mx-auto" />
          <h3 className="text-base font-bold text-white">No Bids Submitted Yet</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            Switch to the Bidder Submission Portal to submit contractor proposals for this tender.
          </p>
        </div>
      ) : hasEvaluated && winner && (
        <div className="space-y-6">
          
          {/* Highlighted Winner Card */}
          <div className="p-6 sm:p-8 rounded-2xl bg-gradient-to-r from-emerald-950/40 via-slate-900 to-slate-900 border-2 border-emerald-500/50 shadow-2xl relative overflow-hidden">
            <div className="absolute top-0 right-0 w-80 h-80 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />

            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/20 border border-emerald-500/30 text-emerald-400 text-xs font-bold">
                <Trophy className="w-4 h-4 text-emerald-400" />
                <span>🥇 RANK 1 RECOMMENDED CONTRACTOR</span>
              </div>

              <div className="px-4 py-1.5 rounded-xl bg-emerald-500 text-slate-950 font-black text-sm shadow-lg shadow-emerald-500/30 flex items-center gap-1.5 self-start">
                <span>TOTAL SCORE: {winner.totalScore} / 100</span>
              </div>
            </div>

            <h3 className="text-2xl font-black text-white tracking-tight">
              {winner.bidderName}
            </h3>

            <div className="flex flex-wrap items-center gap-4 text-xs text-slate-300 mt-2">
              <div>
                <span className="text-slate-400">Commercial Quote: </span>
                <span className="font-bold text-white">₹{winner.totalQuote?.toLocaleString()}</span>
              </div>
              <div>&bull;</div>
              <div>
                <span className="text-slate-400">Proposed Timeline: </span>
                <span className="font-bold text-white">{winner.proposedDays} Days</span>
              </div>
              <div>&bull;</div>
              <div>
                <span className="text-slate-400">Status: </span>
                <span className="font-bold text-emerald-400">{winner.statusNote}</span>
              </div>
            </div>

            <div className="mt-6 pt-5 border-t border-slate-800/80 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <p className="text-xs text-slate-400 max-w-xl">
                Auditable decision: Scores calculated deterministically via published criteria weights and inverse-cost ratio formula.
              </p>

              {activeTender.status === 'AWARDED' ? (
                <div className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 font-bold text-xs">
                  <ShieldCheck className="w-4 h-4" />
                  <span>Contract Officially Awarded & Sealed</span>
                </div>
              ) : (
                <button
                  onClick={() => handleAward(winner)}
                  className="px-5 py-2.5 rounded-xl font-bold text-xs sm:text-sm text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-600/30 transition-all cursor-pointer flex items-center gap-2"
                >
                  <CheckCircle2 className="w-4 h-4" />
                  <span>🏛️ Issuer Approval: Award Official Contract</span>
                </button>
              )}
            </div>
          </div>

          {/* Full Procurement Leaderboard */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
            <h4 className="text-base font-bold text-white flex items-center gap-2">
              <span>Full Procurement Leaderboard & Points Breakdown</span>
              <span className="text-xs text-slate-400 font-normal">({evaluationResults.length} bids evaluated)</span>
            </h4>

            <div className="space-y-3">
              {evaluationResults.map((r) => {
                const isRank1 = r.rank === 1;

                return (
                  <div
                    key={r.bidId}
                    className={`p-4 rounded-xl border transition-all flex flex-col lg:flex-row lg:items-center justify-between gap-4 ${
                      isRank1 
                        ? 'bg-emerald-950/20 border-emerald-500/30 shadow-md' 
                        : 'bg-slate-950 border-slate-800'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <span className={`w-8 h-8 rounded-lg flex items-center justify-center font-black text-sm ${
                        isRank1 ? 'bg-emerald-500 text-slate-950' : r.rank === 2 ? 'bg-slate-700 text-white' : 'bg-slate-800 text-slate-400'
                      }`}>
                        #{r.rank}
                      </span>

                      <div>
                        <div className="text-sm font-bold text-white flex items-center gap-2">
                          <span>{r.bidderName}</span>
                          {isRank1 && <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold">WINNER</span>}
                        </div>
                        <div className="text-xs text-slate-400">
                          Quote: ₹{r.totalQuote?.toLocaleString()} &bull; {r.proposedDays} Days &bull; {r.statusNote}
                        </div>
                      </div>
                    </div>

                    {/* Criteria point badges */}
                    <div className="flex flex-wrap items-center gap-2">
                      {r.criterionScores.map(cs => (
                        <div key={cs.key} className="px-2 py-1 rounded bg-slate-900 border border-slate-800 text-center min-w-[60px]">
                          <div className="text-[9px] uppercase font-bold text-slate-500">{cs.key.substring(0, 4)}</div>
                          <div className="text-xs font-black text-sky-400 font-mono">{cs.weighted}</div>
                        </div>
                      ))}

                      <div className="px-3 py-1.5 rounded-lg bg-indigo-500/15 border border-indigo-500/30 text-center min-w-[75px]">
                        <div className="text-[9px] uppercase font-bold text-indigo-300">TOTAL</div>
                        <div className="text-sm font-black text-white font-mono">{r.totalScore}</div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Deep Audit Trail & Explainability Box */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
            <h4 className="text-base font-bold text-sky-400 flex items-center gap-2">
              <Sparkles className="w-4 h-4" />
              <span>🔍 Deterministic Scoring Audit Trace & Formula Transparency</span>
            </h4>
            <p className="text-xs text-slate-400">
              Why the Rank 1 contractor was recommended: Step-by-step point allocations per approved criterion.
            </p>

            <div className="space-y-2.5">
              {winner.criterionScores.map(cs => (
                <div key={cs.key} className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 text-xs flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="flex items-center gap-2.5">
                    <span className="px-2 py-0.5 rounded font-mono text-[10px] font-bold bg-slate-800 text-indigo-400 border border-slate-700">
                      {cs.key} ({cs.weight}%)
                    </span>
                    <span className="text-slate-300">{cs.explanation}</span>
                  </div>

                  <span className="font-mono font-bold text-emerald-400 shrink-0">
                    +{cs.weighted} pts
                  </span>
                </div>
              ))}
            </div>

            <div className="p-3 rounded-xl bg-indigo-950/20 border border-indigo-500/20 text-xs text-slate-300">
              <span className="font-bold text-indigo-400">Key Engineering Takeaway: </span>
              In high-rainfall terrain, Apex Infrastructure's superior technical pavement (Cement Concrete M-40 + prefabricated drainage) scored +15.5 points in the 35% Technical criterion, outperforming BuildWell's lower commercial price.
            </div>
          </div>

        </div>
      )}

      {/* Contract Awarded Toast Modal */}
      {awardedSuccess && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-emerald-500/40 rounded-2xl p-6 sm:p-8 max-w-md w-full text-center space-y-4 shadow-2xl">
            <div className="w-14 h-14 rounded-full bg-emerald-500/20 text-emerald-400 mx-auto flex items-center justify-center">
              <Trophy className="w-7 h-7" />
            </div>
            <h3 className="text-xl font-bold text-white">Contract Formally Awarded!</h3>
            <p className="text-xs text-slate-400">
              Tender <span className="font-bold text-white">{activeTender.title}</span> has been officially awarded to <span className="font-bold text-emerald-400">{winner?.bidderName}</span>.
            </p>
            <div className="p-3 bg-slate-950 rounded-xl text-xs font-mono text-slate-300">
              Contract Value: ₹{winner?.totalQuote?.toLocaleString()} &bull; Audit Sealed
            </div>
            <button
              onClick={() => setAwardedSuccess(false)}
              className="w-full py-2.5 rounded-xl font-bold text-sm text-white bg-emerald-600 hover:bg-emerald-500 transition-all cursor-pointer"
            >
              Continue Review
            </button>
          </div>
        </div>
      )}

    </div>
  );
}

