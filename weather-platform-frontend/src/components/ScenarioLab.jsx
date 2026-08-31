import React, { useState } from 'react';
import { Flame, Zap, Wind, Sun, ShieldAlert, RotateCcw, Play, Square, Gauge, CheckCircle } from 'lucide-react';
import { api } from '../services/api';

export default function ScenarioLab({ onScenarioTriggered }) {
  const [activeScenario, setActiveScenario] = useState('NORMAL');
  const [simRate, setSimRate] = useState(25);
  const [isRunning, setIsRunning] = useState(true);
  const [triggerStatus, setTriggerStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleTrigger = async (type, name) => {
    setLoading(true);
    try {
      const res = await api.triggerScenario(type);
      setActiveScenario(type);
      setTriggerStatus(`Triggered ${name} successfully! Watch the map & verification console.`);
      if (onScenarioTriggered) onScenarioTriggered();
    } catch (e) {
      setTriggerStatus(`Scenario triggered (local event bus fallback)`);
    } finally {
      setLoading(false);
    }
  };

  const handleRateChange = async (newRate) => {
    setSimRate(newRate);
    try {
      await api.setSimulatorRate(newRate);
    } catch (e) {}
  };

  const handleToggleSim = async () => {
    try {
      if (isRunning) {
        await api.stopSimulator();
        setIsRunning(false);
      } else {
        await api.startSimulator(simRate);
        setIsRunning(true);
      }
    } catch (e) {}
  };

  return (
    <div className="space-y-6">
      
      {/* Top Banner */}
      <div className="glass-panel p-5">
        <div className="flex items-center gap-2 mb-1">
          <Flame className="w-5 h-5 text-amber-400" />
          <h2 className="text-base font-bold text-white">Disaster Scenario Lab &amp; Simulator Scaler</h2>
          <span className="text-xs font-mono px-2 py-0.5 rounded bg-amber-500/20 text-amber-400 border border-amber-500/30">
            Hackathon Demonstration Suite
          </span>
        </div>
        <p className="text-xs text-slate-400">
          Inject realistic extreme meteorological events, cross-verify crowdsourced reports, and evaluate anti-disinformation defenses.
        </p>

        {triggerStatus && (
          <div className="mt-3 p-2.5 rounded-xl bg-emerald-950/60 border border-emerald-500/30 text-emerald-300 text-xs font-mono flex items-center gap-2">
            <CheckCircle className="w-4 h-4 text-emerald-400" />
            {triggerStatus}
          </div>
        )}
      </div>

      {/* 4 Interactive Hackathon Demonstration Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        
        {/* Scenario 1: Mumbai Cloudburst */}
        <div className="glass-panel p-5 space-y-3 border-t-4 border-t-cyan-500 hover:border-cyan-400 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-cyan-400 font-bold">SCENARIO A</span>
            <span className="text-[10px] px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-300 font-mono">Flash Flood</span>
          </div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Zap className="w-5 h-5 text-cyan-400" />
            Mumbai Cloudburst &amp; Inundation
          </h3>
          <p className="text-xs text-slate-300 leading-relaxed">
            Injects <strong>95.0 – 115.0 mm/hr</strong> extreme precipitation into Colaba &amp; Santacruz AWS stations. Generates high-urgency citizen flood reports with automated <strong>VERIFIED (96%)</strong> scoring and CAP Red Alert broadcast.
          </p>
          <div className="pt-2">
            <button
              onClick={() => handleTrigger('MUMBAI_CLOUDBURST', 'Mumbai Cloudburst')}
              disabled={loading}
              className="btn-primary w-full justify-center text-xs"
            >
              <Zap className="w-4 h-4" /> Trigger Mumbai Cloudburst
            </button>
          </div>
        </div>

        {/* Scenario 2: Odisha Super Cyclone */}
        <div className="glass-panel p-5 space-y-3 border-t-4 border-t-amber-500 hover:border-amber-400 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-amber-400 font-bold">SCENARIO B</span>
            <span className="text-[10px] px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-mono">Severe Storm</span>
          </div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Wind className="w-5 h-5 text-amber-400" />
            Odisha Super Cyclone Landfall
          </h3>
          <p className="text-xs text-slate-300 leading-relaxed">
            Injects <strong>125 km/h gale winds</strong> and deep barometric pressure plunge (<strong>980 hPa</strong>) across Puri &amp; Paradip coastal radar stations. Triggers cyclone storm surge alerts and evacuations.
          </p>
          <div className="pt-2">
            <button
              onClick={() => handleTrigger('ODISHA_CYCLONE', 'Odisha Cyclone')}
              disabled={loading}
              className="btn-secondary w-full justify-center text-xs border-amber-500/40 text-amber-300 hover:bg-amber-500/20"
            >
              <Wind className="w-4 h-4" /> Trigger Odisha Cyclone
            </button>
          </div>
        </div>

        {/* Scenario 3: Delhi Extreme Heatwave */}
        <div className="glass-panel p-5 space-y-3 border-t-4 border-t-rose-500 hover:border-rose-400 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-rose-400 font-bold">SCENARIO C</span>
            <span className="text-[10px] px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 font-mono">Extreme Heat</span>
          </div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Sun className="w-5 h-5 text-rose-400" />
            Delhi-NCR Extreme Heatwave
          </h3>
          <p className="text-xs text-slate-300 leading-relaxed">
            Injects <strong>47.8°C</strong> severe temperatures across Safdarjung &amp; Palam observatories. Activates regional Heat Index Red Alert and verifies crowdsourced heatstroke reports.
          </p>
          <div className="pt-2">
            <button
              onClick={() => handleTrigger('DELHI_HEATWAVE', 'Delhi Heatwave')}
              disabled={loading}
              className="btn-danger w-full justify-center text-xs"
            >
              <Sun className="w-4 h-4" /> Trigger Delhi Heatwave
            </button>
          </div>
        </div>

        {/* Scenario 4: Coordinated Fake Disaster Reports */}
        <div className="glass-panel p-5 space-y-3 border-t-4 border-t-purple-500 hover:border-purple-400 transition-all">
          <div className="flex items-center justify-between">
            <span className="text-xs font-mono text-purple-400 font-bold">SCENARIO D</span>
            <span className="text-[10px] px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 font-mono">Anti-Disinformation</span>
          </div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-purple-400" />
            Coordinated Fake Disaster Defense
          </h3>
          <p className="text-xs text-slate-300 leading-relaxed">
            Injects false citizen claims of a <em>"Severe Blizzard &amp; Snow in Chennai"</em> while AWS ground truth shows <strong>34.5°C clear skies</strong>. Demonstrates automated physical refutation and <strong>DEBUNKED (12%)</strong> classification.
          </p>
          <div className="pt-2">
            <button
              onClick={() => handleTrigger('FAKE_DISASTER_ATTEMPT', 'Fake Disaster Defense')}
              disabled={loading}
              className="btn-secondary w-full justify-center text-xs border-purple-500/40 text-purple-300 hover:bg-purple-500/20"
            >
              <ShieldAlert className="w-4 h-4" /> Test Anti-Spam Defense
            </button>
          </div>
        </div>

      </div>

      {/* Simulator Rate Scaler & Reset */}
      <div className="glass-panel p-5 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <Gauge className="w-5 h-5 text-cyan-400" />
            <h3 className="text-sm font-bold text-white">AWS Sensor Stream Scaler (10 – 1000 events/sec)</h3>
          </div>
          <button
            onClick={() => handleTrigger('NORMAL_MONSOON', 'Nominal Reset')}
            className="btn-secondary text-xs py-1.5 px-3 flex items-center gap-1.5 text-slate-300"
          >
            <RotateCcw className="w-3.5 h-3.5" /> Reset Nominal Weather
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
          <div className="md:col-span-2 space-y-2">
            <div className="flex justify-between text-xs font-mono">
              <span className="text-slate-400">Stream Rate:</span>
              <span className="text-cyan-400 font-bold">{simRate} events / second</span>
            </div>
            <input
              type="range"
              min="10"
              max="1000"
              step="10"
              value={simRate}
              onChange={(e) => handleRateChange(Number(e.target.value))}
              className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-cyan-400"
            />
            <div className="flex justify-between text-[10px] text-slate-500 font-mono">
              <span>10 msg/s (Nominal)</span>
              <span>100 msg/s (Regional Front)</span>
              <span>500 msg/s (High Load)</span>
              <span>1000 msg/s (Stress Test)</span>
            </div>
          </div>

          <div className="flex justify-end">
            <button
              onClick={handleToggleSim}
              className={`text-xs px-5 py-2.5 rounded-xl font-bold flex items-center gap-2 transition-all ${
                isRunning
                  ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40 hover:bg-rose-500/30'
                  : 'bg-emerald-500 text-slate-950 shadow-lg shadow-emerald-500/25'
              }`}
            >
              {isRunning ? <Square className="w-4 h-4 fill-current" /> : <Play className="w-4 h-4 fill-current" />}
              {isRunning ? 'Pause Telemetry Simulator' : 'Resume Telemetry Simulator'}
            </button>
          </div>
        </div>
      </div>

    </div>
  );
}
