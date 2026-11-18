import React from 'react';
import { Sparkles, FileText, CheckCircle2, Scale, Clock, IndianRupee, ArrowUpRight, Plus, Send } from 'lucide-react';

export default function Dashboard({ tenders, bids, onNavigate, onSelectTenderForBid, onSelectTenderForEval, currentUser }) {
  const isPublisher = currentUser?.role === 'publisher';
  const isBidder = currentUser?.role === 'bidder';

  const activeTenders = tenders.filter(t => t.status === 'PUBLISHED').length;
  const awardedTenders = tenders.filter(t => t.status === 'AWARDED').length;
  const totalBidsCount = bids.length;

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      
      {/* Top Welcome & Quick Action Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 p-6 rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900/90 to-indigo-950/40 border border-slate-800 shadow-xl relative overflow-hidden">
        <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/10 rounded-full blur-2xl pointer-events-none" />

        <div className="z-10">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 text-xs font-semibold mb-2">
            <span>Logged in as: {currentUser?.name} ({currentUser?.roleTitle})</span>
          </div>
          <h2 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
            Tender Oversight & Evaluation Hub
          </h2>
          <p className="mt-1 text-sm text-slate-400 max-w-2xl">
            LLM-assisted criteria generation with human-in-the-loop review, paired with deterministic rule-based bid evaluation.
          </p>
        </div>

        <div className="flex items-center gap-3 z-10">
          {isPublisher && (
            <button
              onClick={() => onNavigate('designer')}
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-indigo-600 to-sky-600 hover:from-indigo-500 hover:to-sky-500 shadow-lg shadow-indigo-600/25 transition-all cursor-pointer"
            >
              <Plus className="w-4 h-4" />
              <span>Design Tender with AI</span>
            </button>
          )}

          {isBidder && (
            <button
              onClick={() => onNavigate('bids')}
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-600/25 transition-all cursor-pointer"
            >
              <Send className="w-4 h-4" />
              <span>Submit Sealed Bid</span>
            </button>
          )}
        </div>
      </div>

      {/* KPI Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>ACTIVE TENDERS</span>
            <FileText className="w-4 h-4 text-sky-400" />
          </div>
          <div className="text-2xl font-black text-white">{activeTenders}</div>
          <div className="text-[11px] text-slate-500 mt-1">Open for contractor bidding</div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>TOTAL BIDS SUBMITTED</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-black text-emerald-400">{totalBidsCount}</div>
          <div className="text-[11px] text-slate-500 mt-1">Across all infrastructure categories</div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>CONTRACTS AWARDED</span>
            <Sparkles className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-2xl font-black text-indigo-400">{awardedTenders}</div>
          <div className="text-[11px] text-slate-500 mt-1">Officially signed & sealed</div>
        </div>

        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold mb-2">
            <span>SCORING ENGINE</span>
            <Scale className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-2xl font-black text-amber-400">Deterministic</div>
          <div className="text-[11px] text-slate-500 mt-1">100% Math explainable & auditable</div>
        </div>
      </div>

      {/* Main Tenders Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-lg font-bold text-white">Live Procurement Tenders</h3>
            <p className="text-xs text-slate-400">Review specifications, criteria weights, and submitted contractor bids</p>
          </div>
        </div>

        <div className="space-y-3">
          {tenders.map((tender) => {
            const tenderBids = bids.filter(b => b.tenderId === tender.id);
            const isAwarded = tender.status === 'AWARDED';

            return (
              <div
                key={tender.id}
                className="p-5 rounded-xl bg-slate-950/80 border border-slate-800 hover:border-slate-700 transition-all flex flex-col md:flex-row md:items-center justify-between gap-4"
              >
                <div className="space-y-2 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-xs font-bold text-sky-400 font-mono bg-sky-500/10 px-2 py-0.5 rounded border border-sky-500/20">
                      {tender.id}
                    </span>
                    <span className={`text-[10px] font-bold uppercase px-2 py-0.5 rounded border ${
                      isAwarded 
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                        : 'bg-indigo-500/10 text-indigo-400 border-indigo-500/30'
                    }`}>
                      {tender.status}
                    </span>
                    <span className="text-xs text-slate-400">{tender.category}</span>
                  </div>

                  <h4 className="text-base font-bold text-white hover:text-indigo-400 transition-colors">
                    {tender.title}
                  </h4>

                  <p className="text-xs text-slate-400 line-clamp-2 max-w-3xl">
                    {tender.description}
                  </p>

                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 pt-1">
                    <div className="flex items-center gap-1 font-semibold text-slate-300">
                      <IndianRupee className="w-3.5 h-3.5 text-slate-500" />
                      ₹{tender.estimatedBudget?.toLocaleString()} cap
                    </div>
                    <div className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-slate-500" />
                      {tender.maxTimelineDays} days deadline
                    </div>
                    <div className="flex items-center gap-1 text-indigo-400">
                      <Sparkles className="w-3.5 h-3.5" />
                      {tender.criteria?.length || 5} Criteria Defined
                    </div>
                    <div className="flex items-center gap-1 text-emerald-400 font-semibold">
                      <FileText className="w-3.5 h-3.5" />
                      {tenderBids.length} Bids Received
                    </div>
                  </div>

                  {tender.awardedBidderName && (
                    <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs font-bold">
                      🏆 Awarded Winner: {tender.awardedBidderName}
                    </div>
                  )}
                </div>

                {/* Actions */}
                <div className="flex items-center gap-2.5 sm:self-center">
                  <button
                    onClick={() => onSelectTenderForBid(tender.id)}
                    className="px-3.5 py-2 rounded-lg text-xs font-bold text-slate-200 bg-slate-800 hover:bg-slate-700 hover:text-white border border-slate-700 transition-all flex items-center gap-1.5 cursor-pointer"
                  >
                    <Send className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Submit Bid</span>
                  </button>

                  <button
                    onClick={() => onSelectTenderForEval(tender.id)}
                    className="px-4 py-2 rounded-lg text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg shadow-indigo-600/20 transition-all flex items-center gap-1.5 cursor-pointer"
                  >
                    <Scale className="w-3.5 h-3.5" />
                    <span>Evaluation Room</span>
                    <ArrowUpRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

