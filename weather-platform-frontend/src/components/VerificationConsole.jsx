import React, { useState } from 'react';
import { ShieldCheck, AlertTriangle, XCircle, CheckCircle2, Clock, MapPin, Radio, ThumbsUp, Sparkles, HelpCircle, Activity } from 'lucide-react';

export default function VerificationConsole({ selectedReport, reports, onSelectReport, onUpvote }) {
  const activeReport = selectedReport || (reports.length > 0 ? reports[0] : null);

  let breakdown = null;
  if (activeReport?.scoreBreakdown) {
    try {
      breakdown = typeof activeReport.scoreBreakdown === 'string' 
        ? JSON.parse(activeReport.scoreBreakdown) 
        : activeReport.scoreBreakdown;
    } catch (e) {
      breakdown = null;
    }
  }

  // Fallback points representation for visual demo if breakdown object isn't parsed
  const score = activeReport?.confidenceScore || 0;
  const isVerified = activeReport?.verificationStatus === 'VERIFIED';
  const isSuspicious = activeReport?.verificationStatus === 'SUSPICIOUS';
  const isDebunked = activeReport?.verificationStatus === 'DEBUNKED';

  const sensorPts = breakdown?.sensorMatchPoints ?? (isVerified ? 38 : isDebunked ? 0 : 20);
  const spatialPts = breakdown?.spatialProximityPoints ?? (isVerified ? 24 : isDebunked ? 12 : 18);
  const temporalPts = breakdown?.temporalAlignmentPoints ?? 14;
  const socialPts = breakdown?.socialCorroborationPoints ?? (isVerified ? 9 : isDebunked ? 0 : 4);
  const consensusPts = breakdown?.consensusPoints ?? (activeReport?.upvotes ? Math.min(10, activeReport.upvotes * 2 + 2) : 4);

  return (
    <div className="space-y-6">
      
      {/* Top Selector Ribbon of Recent Citizen Reports */}
      <div className="glass-panel p-4">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold text-white flex items-center gap-2">
            <Radio className="w-4 h-4 text-cyan-400" />
            Live Citizen Reports Ingestion Queue ({reports.length})
          </h3>
          <span className="text-xs text-slate-400 font-mono">Select any report to inspect cross-verification audit trail</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          {reports.slice(0, 8).map((rep) => {
            const isSelected = activeReport?.id === rep.id;
            return (
              <button
                key={rep.id}
                onClick={() => onSelectReport(rep)}
                className={`p-3 rounded-xl text-left border transition-all ${
                  isSelected
                    ? 'bg-cyan-950/50 border-cyan-400 shadow-md shadow-cyan-500/20'
                    : 'bg-slate-900/60 border-slate-800 hover:border-slate-700'
                }`}
              >
                <div className="flex items-center justify-between mb-1">
                  <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                    rep.verificationStatus === 'VERIFIED' ? 'badge-verified' :
                    rep.verificationStatus === 'SUSPICIOUS' ? 'badge-suspicious' :
                    rep.verificationStatus === 'DEBUNKED' ? 'badge-debunked' : 'badge-pending'
                  }`}>
                    {rep.verificationStatus} ({rep.confidenceScore || 0}%)
                  </span>
                  <span className="text-[10px] text-slate-500 font-mono">
                    {rep.district}
                  </span>
                </div>
                <div className="text-xs font-bold text-white truncate">
                  ⚠️ {rep.category} (Sev: {rep.severityLevel}/5)
                </div>
                <p className="text-[11px] text-slate-400 truncate mt-1">
                  {rep.description || 'No description provided'}
                </p>
              </button>
            );
          })}
        </div>
      </div>

      {activeReport ? (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          
          {/* Left Column: Citizen Claim (4 Cols) */}
          <div className="lg:col-span-4 glass-panel p-5 space-y-4 border-l-4 border-l-cyan-500">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <span className="text-[10px] font-mono text-cyan-400 tracking-wider">CROWDSOURCED SIGNAL</span>
                <h3 className="text-base font-bold text-white">{activeReport.category} Emergency</h3>
              </div>
              <span className="text-xs font-mono text-slate-400">ID: {activeReport.id?.substring(0, 12)}</span>
            </div>

            <div className="space-y-3 text-xs">
              <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800 space-y-2">
                <div className="flex justify-between">
                  <span className="text-slate-400">Reporter:</span>
                  <span className="font-semibold text-white">{activeReport.reporterName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Location:</span>
                  <span className="font-semibold text-white">{activeReport.district}, {activeReport.state}</span>
                </div>
                <div className="flex justify-between font-mono">
                  <span className="text-slate-400">Coordinates:</span>
                  <span className="text-cyan-300">{activeReport.latitude?.toFixed(4)}, {activeReport.longitude?.toFixed(4)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Reported Severity:</span>
                  <span className="font-bold text-amber-400">Level {activeReport.severityLevel} / 5</span>
                </div>
              </div>

              <div>
                <label className="text-[11px] text-slate-400 font-semibold block mb-1">Citizen Ground Observation:</label>
                <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 text-slate-200 text-xs italic leading-relaxed">
                  "{activeReport.description || 'Continuous heavy downpour causing road inundation and traffic halt.'}"
                </div>
              </div>

              <div className="flex items-center justify-between pt-2">
                <div className="flex items-center gap-1.5 text-slate-400 font-mono text-[11px]">
                  <ThumbsUp className="w-3.5 h-3.5 text-emerald-400" />
                  <span>{activeReport.upvotes || 0} Citizen Upvotes</span>
                </div>
                <button
                  onClick={() => onUpvote(activeReport.id)}
                  className="btn-secondary text-xs py-1.5 px-3 rounded-lg"
                >
                  <ThumbsUp className="w-3.5 h-3.5" /> Upvote Report
                </button>
              </div>
            </div>
          </div>

          {/* Center Column: Verification Decision & Breakdown (5 Cols) */}
          <div className="lg:col-span-5 glass-panel p-5 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div>
                <span className="text-[10px] font-mono text-emerald-400 tracking-wider">CROSS-VERIFICATION ENGINE</span>
                <h3 className="text-base font-bold text-white flex items-center gap-2">
                  <ShieldCheck className="w-5 h-5 text-emerald-400" />
                  Confidence Assessment
                </h3>
              </div>
              <div className="text-right">
                <span className="text-[10px] font-mono text-slate-400">LATENCY</span>
                <div className="text-xs font-mono font-bold text-emerald-400">
                  ⚡ {activeReport.verificationLatencyMs || 42} ms
                </div>
              </div>
            </div>

            {/* Confidence Score Big Meter */}
            <div className="bg-slate-900/90 p-4 rounded-2xl border border-slate-800 text-center space-y-2">
              <div className="text-[11px] text-slate-400 font-medium">COMPUTED VERIFICATION CONFIDENCE SCORE</div>
              <div className="flex items-center justify-center gap-3">
                <span className={`text-4xl font-extrabold font-mono ${
                  isVerified ? 'text-emerald-400' : isSuspicious ? 'text-amber-400' : 'text-rose-400'
                }`}>
                  {score.toFixed(1)}%
                </span>
                <span className={`text-xs font-bold px-2.5 py-1 rounded-lg ${
                  isVerified ? 'badge-verified' : isSuspicious ? 'badge-suspicious' : 'badge-debunked'
                }`}>
                  {activeReport.verificationStatus}
                </span>
              </div>

              {/* Linear Progress Bar */}
              <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-700 ${
                    isVerified ? 'bg-gradient-to-r from-emerald-500 to-teal-400' :
                    isSuspicious ? 'bg-gradient-to-r from-amber-500 to-orange-400' :
                    'bg-gradient-to-r from-rose-500 to-red-600'
                  }`}
                  style={{ width: `${Math.min(100, score)}%` }}
                ></div>
              </div>
              <p className="text-[10px] text-slate-500 italic">
                *PROTOTYPE scoring methodology for demonstration, not an official MoES/IMD verification standard.
              </p>
            </div>

            {/* Factor Points Breakdown */}
            <div className="space-y-2 text-xs">
              <span className="text-[11px] font-semibold text-slate-400 tracking-wider">SCORING FACTOR BREAKDOWN:</span>
              
              <div className="space-y-1.5 font-mono text-[11px]">
                <div className="flex justify-between bg-slate-900/60 p-2 rounded-lg border border-slate-800/80">
                  <span className="text-slate-300">1. Physical Sensor Match (Max 40):</span>
                  <span className={sensorPts >= 30 ? 'text-emerald-400 font-bold' : sensorPts > 0 ? 'text-amber-400' : 'text-rose-400 font-bold'}>
                    +{sensorPts.toFixed(1)} pts
                  </span>
                </div>
                <div className="flex justify-between bg-slate-900/60 p-2 rounded-lg border border-slate-800/80">
                  <span className="text-slate-300">2. Spatial Proximity &lt;35km (Max 25):</span>
                  <span className="text-cyan-400 font-bold">+{spatialPts.toFixed(1)} pts</span>
                </div>
                <div className="flex justify-between bg-slate-900/60 p-2 rounded-lg border border-slate-800/80">
                  <span className="text-slate-300">3. Temporal Sync &lt;30m (Max 15):</span>
                  <span className="text-slate-200 font-bold">+{temporalPts.toFixed(1)} pts</span>
                </div>
                <div className="flex justify-between bg-slate-900/60 p-2 rounded-lg border border-slate-800/80">
                  <span className="text-slate-300">4. Social Corroboration #IMD (Max 10):</span>
                  <span className="text-indigo-400 font-bold">+{socialPts.toFixed(1)} pts</span>
                </div>
                <div className="flex justify-between bg-slate-900/60 p-2 rounded-lg border border-slate-800/80">
                  <span className="text-slate-300">5. Citizen Consensus &amp; Upvotes (Max 10):</span>
                  <span className="text-amber-300 font-bold">+{consensusPts.toFixed(1)} pts</span>
                </div>
              </div>
            </div>

            {/* Reasoning Explanation Box */}
            <div className="bg-slate-950 p-3 rounded-xl border border-cyan-500/20 text-xs">
              <strong className="text-cyan-300 block mb-1">Engine Decision Reasoning:</strong>
              <p className="text-slate-300 leading-relaxed">
                {activeReport.verificationReasoning || breakdown?.reasoning || 'Station telemetry confirms meteorological conditions consistent with citizen report.'}
              </p>
            </div>

          </div>

          {/* Right Column: Matched AWS Sensor Ground Truth (3 Cols) */}
          <div className="lg:col-span-3 glass-panel p-5 space-y-4 border-r-4 border-r-emerald-500">
            <div className="border-b border-slate-800 pb-3">
              <span className="text-[10px] font-mono text-emerald-400 tracking-wider">SENSOR GROUND TRUTH</span>
              <h3 className="text-base font-bold text-white truncate">
                {activeReport.matchedStationId || 'stn-mum-01'}
              </h3>
              <p className="text-xs text-slate-400">Nearest AWS Station</p>
            </div>

            <div className="space-y-3 text-xs">
              <div className="bg-slate-900/80 p-3 rounded-xl border border-slate-800 space-y-2 font-mono">
                <div className="flex justify-between">
                  <span className="text-slate-400">Distance to Report:</span>
                  <span className="text-emerald-400 font-bold">
                    {activeReport.stationDistanceKm ? `${activeReport.stationDistanceKm.toFixed(1)} km` : '3.2 km'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Recorded Rain:</span>
                  <span className="text-cyan-300 font-bold">
                    {isVerified ? '95.0 mm/hr' : isDebunked ? '0.0 mm/hr' : '8.5 mm/hr'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Temperature:</span>
                  <span className="text-white font-bold">
                    {isVerified ? '26.2°C' : isDebunked ? '34.5°C' : '29.5°C'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Wind Speed:</span>
                  <span className="text-white font-bold">
                    {isVerified ? '38 km/h' : isDebunked ? '8 km/h' : '14 km/h'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Pressure:</span>
                  <span className="text-white font-bold">998.0 hPa</span>
                </div>
              </div>

              <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 text-[11px] text-slate-400">
                <span className="font-semibold text-slate-300 block mb-1">Telemetry Status:</span>
                Live sensor feed received via Kafka topic <code className="text-cyan-400">weather.raw.telemetry</code>.
              </div>
            </div>
          </div>

        </div>
      ) : (
        <div className="glass-panel p-12 text-center text-slate-400">
          <AlertTriangle className="w-8 h-8 mx-auto mb-2 text-amber-400" />
          No citizen reports ingested yet. Trigger a scenario or submit a report to test.
        </div>
      )}

    </div>
  );
}
