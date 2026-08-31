import React from 'react';
import { CloudRain, ShieldCheck, Activity, Database, Flame, Plus, Radio, AlertTriangle } from 'lucide-react';

export default function Header({ activeTab, setActiveTab, stats, onOpenReportModal, activeAlertsCount }) {
  return (
    <header className="glass-header px-6 py-3.5">
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
        
        {/* Left: Branding & Emblems */}
        <div className="flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/30">
            <CloudRain className="w-6 h-6 text-slate-950 font-bold" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold tracking-wider px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">
                MoES • IMD
              </span>
              <span className="text-xs font-mono text-slate-400">SIH26069 PROTOTYPE</span>
            </div>
            <h1 className="text-lg font-bold text-white tracking-tight flex items-center gap-2">
              National Weather Big Data Analytics Platform
            </h1>
          </div>
        </div>

        {/* Center: Live Kafka Event Backbone Ticker */}
        <div className="flex items-center gap-4 bg-slate-900/80 border border-cyan-500/20 px-3.5 py-1.5 rounded-full text-xs font-mono">
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 pulse-live"></span>
            <span className="text-emerald-400 font-semibold">EVENT BACKBONE LIVE</span>
          </div>
          <span className="text-slate-600">|</span>
          <span className="text-slate-300">
            Throughput: <strong className="text-cyan-400 font-semibold">{stats?.currentIngestionRateEventsSec || 28}</strong> msg/s
          </span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-300">
            Avg Latency: <strong className="text-emerald-400 font-semibold">{stats?.averageVerificationLatencyMs || 42}</strong> ms
          </span>
          {activeAlertsCount > 0 && (
            <>
              <span className="text-slate-600">|</span>
              <div className="flex items-center gap-1.5 text-rose-400 font-semibold">
                <AlertTriangle className="w-3.5 h-3.5 animate-bounce" />
                <span>{activeAlertsCount} ACTIVE ALERTS</span>
              </div>
            </>
          )}
        </div>

        {/* Right: Tab Navigation & Report Button */}
        <div className="flex items-center gap-2">
          <nav className="flex items-center bg-slate-900/90 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('map')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === 'map' ? 'bg-cyan-500 text-slate-950 font-bold shadow-md shadow-cyan-500/20' : 'text-slate-300 hover:text-white'
              }`}
            >
              <Activity className="w-3.5 h-3.5" /> Map View
            </button>
            <button
              onClick={() => setActiveTab('verify')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === 'verify' ? 'bg-cyan-500 text-slate-950 font-bold shadow-md shadow-cyan-500/20' : 'text-slate-300 hover:text-white'
              }`}
            >
              <ShieldCheck className="w-3.5 h-3.5" /> Verification Console
            </button>
            <button
              onClick={() => setActiveTab('analytics')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === 'analytics' ? 'bg-cyan-500 text-slate-950 font-bold shadow-md shadow-cyan-500/20' : 'text-slate-300 hover:text-white'
              }`}
            >
              <Database className="w-3.5 h-3.5" /> ClickHouse OLAP
            </button>
            <button
              onClick={() => setActiveTab('scenario')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 ${
                activeTab === 'scenario' ? 'bg-amber-500 text-slate-950 font-bold shadow-md shadow-amber-500/20' : 'text-slate-300 hover:text-white'
              }`}
            >
              <Flame className="w-3.5 h-3.5" /> Scenario Lab
            </button>
          </nav>

          <button
            onClick={onOpenReportModal}
            className="btn-primary text-xs py-2 px-3.5 rounded-xl shadow-lg shadow-cyan-500/25"
          >
            <Plus className="w-4 h-4" /> Report Disaster
          </button>
        </div>

      </div>
    </header>
  );
}
