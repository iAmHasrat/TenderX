import React, { useState, useEffect } from 'react';
import { generateCriteriaWithLLM, validateCriteriaWeights, generateCriteriaFromDescription } from '../services/aiService';
import { Sparkles, ArrowLeft, CheckCircle2, Sliders, FilePlus, Check, Key, Bot, Settings2, Trash2 } from 'lucide-react';

export default function AITenderDesigner({ onPublishTender, onCancel }) {
  // Form input state
  const [title, setTitle] = useState('NH-48 Coastal Highway Drainage & Pavement Stabilization');
  const [category, setCategory] = useState('Civil & Road Construction');
  const [description, setDescription] = useState(
    'Repair a 3.5 km sector of National Highway 48 passing through a heavy-rainfall Ghat area prone to severe waterlogging. The road carries over 14,000 commercial heavy axle vehicles daily. Work must finish within 60 days before the peak monsoon. Pavement durability and storm drainage are critical.'
  );
  const [budget, setBudget] = useState('2500000');
  const [timelineDays, setTimelineDays] = useState('60');

  // LLM API configuration
  const [llmProvider, setLlmProvider] = useState(() => localStorage.getItem('tenderx_llm_provider') || 'gemini');
  const [apiKey, setApiKey] = useState(() => localStorage.getItem('tenderx_llm_key') || '');
  const [showApiSettings, setShowApiSettings] = useState(false);
  const [llmStatusNote, setLlmStatusNote] = useState('');

  // AI Generation state
  const [isGenerating, setIsGenerating] = useState(false);
  const [criteria, setCriteria] = useState(null);
  const [publishedSuccess, setPublishedSuccess] = useState(false);

  // New field modal state
  const [activeCritKey, setActiveCritKey] = useState(null);
  const [newFieldLabel, setNewFieldLabel] = useState('');
  const [newFieldType, setNewFieldType] = useState('TEXT');
  const [newFieldOptions, setNewFieldOptions] = useState('');

  useEffect(() => {
    localStorage.setItem('tenderx_llm_provider', llmProvider);
  }, [llmProvider]);

  useEffect(() => {
    localStorage.setItem('tenderx_llm_key', apiKey);
  }, [apiKey]);

  // Quick Scenarios
  const loadScenario = (type) => {
    if (type === 'monsoon') {
      setTitle('NH-48 Monsoon Hill Corridor Pavement & Drainage Restoration');
      setCategory('Civil & Road Construction');
      setDescription('Repair a 3.5 km sector of National Highway 48 passing through a heavy-rainfall Ghat area prone to severe waterlogging. The road carries over 14,000 commercial heavy axle vehicles daily. Work must finish within 60 days before the peak monsoon. Pavement durability and storm drainage are critical.');
      setBudget('2500000');
      setTimelineDays('60');
    } else if (type === 'solar') {
      setTitle('District Memorial Hospital 200kW Rooftop Solar Captive System');
      setCategory('Solar & Renewable Energy');
      setDescription('Turnkey installation of a 200 kWp rooftop solar photovoltaic system for the hospital campus. Must integrate Tier-1 high efficiency solar PV modules, string inverters with net-metering grid synchronization, and electrical safety certifications. Complete within 90 days.');
      setBudget('9500000');
      setTimelineDays('90');
    } else if (type === 'paint') {
      setTitle('Civil Secretariat Administrative Complex External Repainting');
      setCategory('Building Maintenance & Painting');
      setDescription('Routine exterior repainting and crack sealing for 4 institutional multi-storey blocks. Work involves scaffolding at heights, weather-proof elastomeric coating, and high cost economy. Must finish in 45 days.');
      setBudget('650000');
      setTimelineDays('45');
    }
  };

  const handleGenerateAI = async () => {
    setIsGenerating(true);
    setLlmStatusNote('');

    try {
      const generated = await generateCriteriaWithLLM({
        description,
        category,
        budget: Number(budget),
        days: Number(timelineDays),
        apiKey: apiKey.trim(),
        provider: llmProvider
      });

      setCriteria(generated);
      if (apiKey.trim()) {
        setLlmStatusNote(`✅ Generated via live ${llmProvider.toUpperCase()} API response!`);
      } else {
        setLlmStatusNote('⚡ Generated via Built-in Intelligent Schema Engine (Enter API Key in Settings for live Gemini/Grok).');
      }
    } catch (err) {
      console.error("AI Generation error:", err);
      // Fallback safely to heuristic engine so UI never breaks
      const fallback = generateCriteriaFromDescription({
        description,
        category,
        budget: Number(budget),
        days: Number(timelineDays)
      });
      setCriteria(fallback);
      setLlmStatusNote(`⚠️ Live ${llmProvider.toUpperCase()} Error: ${err.message || 'Call failed'}. Switched to Smart Heuristic Engine.`);
    } finally {
      setIsGenerating(false);
    }
  };

  const handleWeightChange = (key, newWeight) => {
    setCriteria(prev => prev.map(c => c.key === key ? { ...c, weight: Number(newWeight) } : c));
  };

  const handleAddField = (critKey) => {
    if (!newFieldLabel.trim()) return;
    const newField = {
      id: 'fld_' + Date.now(),
      key: newFieldLabel.toLowerCase().replace(/[^a-z0-9]/g, '_'),
      label: newFieldLabel.trim(),
      type: newFieldType,
      options: newFieldType === 'ENUM' ? newFieldOptions.split(',').map(s => s.trim()).filter(Boolean) : null,
      required: true
    };

    setCriteria(prev => prev.map(c => {
      if (c.key === critKey) {
        return { ...c, fields: [...c.fields, newField] };
      }
      return c;
    }));

    setNewFieldLabel('');
    setNewFieldOptions('');
    setActiveCritKey(null);
  };

  const handleRemoveField = (critKey, fieldId) => {
    setCriteria(prev => prev.map(c => {
      if (c.key === critKey) {
        return { ...c, fields: c.fields.filter(f => f.id !== fieldId) };
      }
      return c;
    }));
  };

  const weightValidation = criteria ? validateCriteriaWeights(criteria) : { valid: false };

  const handlePublish = () => {
    if (!weightValidation.valid) return;

    const newTender = {
      id: 'TND-' + Math.floor(1000 + Math.random() * 9000),
      title: title.trim(),
      category,
      description: description.trim(),
      estimatedBudget: Number(budget),
      maxTimelineDays: Number(timelineDays),
      status: 'PUBLISHED',
      createdBy: 'Department Tender Authority',
      createdAt: new Date().toISOString().replace('T', ' ').substring(0, 16),
      criteria
    };

    onPublishTender(newTender);
    setPublishedSuccess(true);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <button
            onClick={onCancel}
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-400 hover:text-white mb-2 cursor-pointer"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            Back to Dashboard
          </button>
          <h2 className="text-2xl font-black text-white tracking-tight flex items-center gap-2">
            <Sparkles className="w-6 h-6 text-sky-400" />
            AI Tender Specification & Criteria Designer
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Steps 1 to 4: Describe project in natural language &rarr; LLM proposes 5 criteria & generates dynamic form &rarr; Review, customize fields, and approve.
          </p>
        </div>

        {/* LLM Engine Toggle Button */}
        <button
          onClick={() => setShowApiSettings(!showApiSettings)}
          className="inline-flex items-center gap-2 px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs font-semibold text-slate-300 self-start sm:self-auto cursor-pointer"
        >
          <Settings2 className="w-3.5 h-3.5 text-indigo-400" />
          <span>LLM Engine Settings {apiKey ? '(Active Key)' : '(Offline Mode)'}</span>
        </button>
      </div>

      {/* LLM API Settings Card (Expandable) */}
      {showApiSettings && (
        <div className="p-5 rounded-2xl bg-indigo-950/20 border border-indigo-500/30 space-y-4 animate-in fade-in duration-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Bot className="w-4 h-4 text-indigo-400" />
              <span className="text-xs font-bold text-white uppercase tracking-wider">Live LLM API Configuration</span>
            </div>
            <span className="text-[11px] text-slate-400">Zero-setup fallback active if key is blank</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Model Provider</label>
              <select
                value={llmProvider}
                onChange={(e) => setLlmProvider(e.target.value)}
                className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-800 text-xs text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
              >
                <option value="gemini">Google Gemini (gemini-1.5-flash)</option>
                <option value="grok">xAI Grok (grok-beta)</option>
              </select>
            </div>

            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-300 mb-1">API Key (Optional for live LLM requests)</label>
              <div className="relative">
                <input
                  type="password"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder={`Paste your ${llmProvider === 'gemini' ? 'Gemini' : 'Grok'} API key here...`}
                  className="w-full pl-8 pr-3 py-2 rounded-lg bg-slate-900 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                <Key className="w-3.5 h-3.5 text-slate-500 absolute left-2.5 top-2.5" />
              </div>
            </div>
          </div>

          <p className="text-[11px] text-slate-400">
            💡 If no key is entered, TenderX automatically uses the built-in deterministic schema intelligence engine so your video recording demo runs instantly with 0 ms latency!
          </p>
        </div>
      )}

      {/* Input Form Card */}
      <div className="p-6 sm:p-8 rounded-2xl bg-slate-900 border border-slate-800 shadow-xl space-y-6">
        
        {/* Quick Scenario Chips */}
        <div>
          <label className="block text-xs font-bold uppercase tracking-wider text-sky-400 mb-2">
            Quick Test Scenarios (Click to Pre-fill):
          </label>
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={() => loadScenario('monsoon')}
              className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 hover:border-indigo-500 transition-all cursor-pointer"
            >
              🌧️ Scenario A: Road in High-Rainfall Area
            </button>
            <button
              type="button"
              onClick={() => loadScenario('solar')}
              className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 hover:border-indigo-500 transition-all cursor-pointer"
            >
              ☀️ Scenario B: Hospital Solar Rooftop
            </button>
            <button
              type="button"
              onClick={() => loadScenario('paint')}
              className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-800/80 hover:bg-slate-700 text-slate-200 border border-slate-700 hover:border-indigo-500 transition-all cursor-pointer"
            >
              🎨 Scenario C: Routine Building Repainting
            </button>
          </div>
        </div>

        {/* Title & Category Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2">
            <label className="block text-xs font-bold text-slate-300 mb-1.5">
              Tender Project Title *
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              placeholder="e.g. State Highway Monsoon Reconstruction"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1.5">
              Procurement Category *
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
            >
              <option value="Civil & Road Construction">Civil & Road Construction</option>
              <option value="Solar & Renewable Energy">Solar & Renewable Energy</option>
              <option value="Building Maintenance & Painting">Building Maintenance & Painting</option>
              <option value="IT Infrastructure & Networking">IT Infrastructure & Networking</option>
            </select>
          </div>
        </div>

        {/* Natural Language Scope (Typeable Requirements) */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label className="block text-xs font-bold text-slate-300">
              Natural Language Project Description & Technical Requirements *
            </label>
            <span className="text-[11px] text-slate-500 font-mono">
              {description.length} characters &bull; {description.split(/\s+/).filter(Boolean).length} words
            </span>
          </div>
          <textarea
            rows={4}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
            placeholder="Type your project scope, weather constraints, traffic load, or special materials needed here..."
          />
        </div>

        {/* Budget & Timeline */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1.5">
              Estimated Budget Cap (₹) *
            </label>
            <input
              type="number"
              value={budget}
              onChange={(e) => setBudget(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1.5">
              Mandatory Completion Deadline (Days) *
            </label>
            <input
              type="number"
              value={timelineDays}
              onChange={(e) => setTimelineDays(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-lg bg-slate-950 border border-slate-800 text-sm text-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
            />
          </div>
        </div>

        {/* AI Generate Button */}
        <div className="pt-2 flex flex-col sm:flex-row sm:items-center gap-3">
          <button
            type="button"
            onClick={handleGenerateAI}
            disabled={isGenerating || !description.trim()}
            className="flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-sky-500 via-indigo-600 to-emerald-500 hover:from-sky-400 hover:to-emerald-400 shadow-lg shadow-indigo-600/25 transition-all cursor-pointer disabled:opacity-50"
          >
            <Sparkles className="w-4 h-4" />
            <span>{isGenerating ? 'AI Model Analyzing Constraints...' : '✨ Generate 5 Criteria + Dynamic Form with AI'}</span>
          </button>

          {llmStatusNote && (
            <span className="text-xs text-emerald-400 font-semibold bg-emerald-500/10 px-3 py-1.5 rounded-lg border border-emerald-500/20">
              {llmStatusNote}
            </span>
          )}
        </div>

      </div>

      {/* Generated Criteria & Dynamic Requirements Section */}
      {criteria && (
        <div className="space-y-6">
          
          {/* Real-time Weight Validation Bar */}
          <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-4 sticky top-20 z-40 shadow-2xl backdrop-blur-md">
            <div className="flex items-center gap-3">
              <div className="text-xs font-bold uppercase tracking-wider text-slate-400">
                Rule Check: Criteria Weights
              </div>
              <div className={`px-3 py-1 rounded-full text-xs font-black ${
                weightValidation.valid ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' : 'bg-rose-500/15 text-rose-400 border border-rose-500/30'
              }`}>
                Total: {weightValidation.total}%
              </div>
              <span className="text-xs text-slate-400 hidden md:inline">
                {weightValidation.valid ? '✔ Deterministic math rule satisfied' : '⚠️ Weights must total exactly 100%'}
              </span>
            </div>

            <button
              onClick={handlePublish}
              disabled={!weightValidation.valid}
              className="flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm text-white bg-emerald-600 hover:bg-emerald-500 shadow-lg shadow-emerald-600/20 disabled:opacity-40 transition-all cursor-pointer"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Approve & Publish Official Tender</span>
            </button>
          </div>

          {/* 5 Criteria Cards */}
          <div className="space-y-4">
            {criteria.map((c, idx) => (
              <div
                key={c.key}
                className="p-6 rounded-2xl bg-slate-900 border border-slate-800 shadow-lg space-y-4"
              >
                {/* Criterion Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <span className="w-7 h-7 rounded-lg bg-indigo-500/15 border border-indigo-500/30 text-indigo-400 flex items-center justify-center text-xs font-black font-mono">
                      {idx + 1}
                    </span>
                    <div>
                      <h4 className="text-base font-bold text-white">{c.name}</h4>
                      <span className="text-[10px] uppercase font-bold tracking-wider text-slate-500 font-mono">
                        Key: {c.key}
                      </span>
                    </div>
                  </div>

                  {/* Weight Slider */}
                  <div className="flex items-center gap-3 bg-slate-950 px-3.5 py-1.5 rounded-xl border border-slate-800">
                    <Sliders className="w-3.5 h-3.5 text-slate-400" />
                    <span className="text-xs font-semibold text-slate-400">Weight:</span>
                    <input
                      type="range"
                      min={5}
                      max={60}
                      step={5}
                      value={c.weight}
                      onChange={(e) => handleWeightChange(c.key, e.target.value)}
                      className="w-24 sm:w-32 accent-indigo-500 cursor-pointer"
                    />
                    <span className="text-sm font-black text-sky-400 min-w-[40px] text-right font-mono">
                      {c.weight}%
                    </span>
                  </div>
                </div>

                {/* AI Rationale Note */}
                {c.rationale && (
                  <div className="p-3 rounded-xl bg-sky-950/20 border border-sky-500/20 text-xs text-slate-300 flex items-start gap-2.5">
                    <Sparkles className="w-4 h-4 text-sky-400 shrink-0 mt-0.5" />
                    <div>
                      <span className="font-bold text-sky-300">AI Proposed Weight Rationale: </span>
                      <span>{c.rationale}</span>
                    </div>
                  </div>
                )}

                {/* Dynamic Requirements / Fields List */}
                <div className="space-y-2 pt-2">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-400">
                      Generated Bidder Requirements & Input Fields ({c.fields.length}):
                    </span>

                    <button
                      type="button"
                      onClick={() => setActiveCritKey(c.key)}
                      className="inline-flex items-center gap-1 text-xs font-bold text-indigo-400 hover:text-indigo-300 cursor-pointer"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      Add Custom Requirement
                    </button>
                  </div>

                  <div className="space-y-2">
                    {c.fields.map(f => (
                      <div
                        key={f.id}
                        className="p-3 rounded-xl bg-slate-950 border border-slate-800 flex items-center justify-between gap-3 text-xs"
                      >
                        <div className="flex items-center gap-2.5">
                          <span className="px-2 py-0.5 rounded font-mono text-[10px] font-bold bg-slate-800 text-sky-400 border border-slate-700">
                            {f.type}
                          </span>
                          <span className="font-semibold text-slate-200">{f.label}</span>
                          {f.options && (
                            <span className="text-[11px] text-slate-500 hidden sm:inline">
                              [{f.options.join(', ')}]
                            </span>
                          )}
                          {f.unit && (
                            <span className="text-[11px] text-slate-500 font-mono">
                              ({f.unit})
                            </span>
                          )}
                        </div>

                        <button
                          type="button"
                          onClick={() => handleRemoveField(c.key, f.id)}
                          className="text-slate-500 hover:text-rose-400 p-1 cursor-pointer transition-colors"
                          title="Remove requirement field"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>

                  {/* Inline Add Requirement Box */}
                  {activeCritKey === c.key && (
                    <div className="p-4 rounded-xl bg-indigo-950/20 border border-indigo-500/30 space-y-3 mt-3">
                      <div className="text-xs font-bold text-indigo-300 flex items-center gap-1.5">
                        <FilePlus className="w-3.5 h-3.5" />
                        Add Custom Tender Requirement to {c.name}
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                        <div className="sm:col-span-2">
                          <input
                            type="text"
                            value={newFieldLabel}
                            onChange={(e) => setNewFieldLabel(e.target.value)}
                            placeholder="e.g. Core Sample Density Verification Protocol"
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-700 text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                          />
                        </div>

                        <div>
                          <select
                            value={newFieldType}
                            onChange={(e) => setNewFieldType(e.target.value)}
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-700 text-xs text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                          >
                            <option value="TEXT">Text Field</option>
                            <option value="NUMBER">Number Field</option>
                            <option value="ENUM">Dropdown Options</option>
                            <option value="BOOLEAN">Yes / No Checkbox</option>
                          </select>
                        </div>
                      </div>

                      {newFieldType === 'ENUM' && (
                        <div>
                          <input
                            type="text"
                            value={newFieldOptions}
                            onChange={(e) => setNewFieldOptions(e.target.value)}
                            placeholder="Options separated by comma, e.g. Grade A, Grade B, Alternative"
                            className="w-full px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-700 text-xs text-white placeholder-slate-500 focus:outline-none"
                          />
                        </div>
                      )}

                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleAddField(c.key)}
                          className="px-3 py-1 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-xs font-bold text-white cursor-pointer"
                        >
                          Confirm Field
                        </button>
                        <button
                          type="button"
                          onClick={() => setActiveCritKey(null)}
                          className="px-3 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-400 cursor-pointer"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  )}

                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Success Modal */}
      {publishedSuccess && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-emerald-500/40 rounded-2xl p-6 sm:p-8 max-w-md w-full text-center space-y-4 shadow-2xl">
            <div className="w-12 h-12 rounded-full bg-emerald-500/20 text-emerald-400 mx-auto flex items-center justify-center">
              <Check className="w-6 h-6" />
            </div>
            <h3 className="text-xl font-bold text-white">Tender Published Successfully!</h3>
            <p className="text-xs text-slate-400">
              The 5 criteria, dynamic response form, and weights have been officially approved and locked into the deterministic evaluation database.
            </p>
            <button
              onClick={onCancel}
              className="w-full py-2.5 rounded-xl font-bold text-sm text-white bg-emerald-600 hover:bg-emerald-500 transition-all cursor-pointer"
            >
              Return to Dashboard
            </button>
          </div>
        </div>
      )}

    </div>
  );
}
