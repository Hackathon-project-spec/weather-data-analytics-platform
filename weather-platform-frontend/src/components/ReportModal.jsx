import React, { useState } from 'react';
import { X, ShieldCheck, Send, Sparkles, MapPin, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { api } from '../services/api';

const CITY_PRESETS = [
  { name: 'Mumbai (Dadar)', state: 'Maharashtra', district: 'Mumbai City', lat: 18.9100, lon: 72.8200 },
  { name: 'Mumbai (Kurla)', state: 'Maharashtra', district: 'Mumbai Suburban', lat: 19.0760, lon: 72.8777 },
  { name: 'Delhi (Connaught Place)', state: 'Delhi', district: 'New Delhi', lat: 28.6304, lon: 77.2177 },
  { name: 'Puri (Beach Road)', state: 'Odisha', district: 'Puri', lat: 19.8135, lon: 85.8312 },
  { name: 'Bengaluru (Indiranagar)', state: 'Karnataka', district: 'Bengaluru Urban', lat: 12.9716, lon: 77.6412 },
  { name: 'Chennai (Marina Beach)', state: 'Tamil Nadu', district: 'Chennai', lat: 13.0600, lon: 80.2800 },
  { name: 'Kolkata (Park Street)', state: 'West Bengal', district: 'Kolkata', lat: 22.5500, lon: 88.3500 },
];

export default function ReportModal({ isOpen, onClose, onReportSubmitted }) {
  const [formData, setFormData] = useState({
    reporterName: 'Ananya Deshmukh',
    category: 'FLOOD',
    severityLevel: 4,
    state: 'Maharashtra',
    district: 'Mumbai City',
    latitude: 18.9100,
    longitude: 72.8200,
    description: 'Severe waterlogging near flyover, water level rising rapidly above 2.5 feet.',
  });

  const [previewResult, setPreviewResult] = useState(null);
  const [evaluating, setEvaluating] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  if (!isOpen) return null;

  const handleCitySelect = (city) => {
    setFormData(prev => ({
      ...prev,
      state: city.state,
      district: city.district,
      latitude: city.lat,
      longitude: city.lon
    }));
    setPreviewResult(null);
  };

  const handleDryRunEvaluate = async () => {
    setEvaluating(true);
    try {
      const res = await api.evaluateReportDirectly({
        ...formData,
        reportId: 'preview-rep-' + Date.now(),
        upvotes: 1
      });
      setPreviewResult(res.data);
    } catch (e) {
      console.error("Dry run error:", e);
    } finally {
      setEvaluating(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const res = await api.submitReport({
        ...formData,
        upvotes: 1
      });
      if (onReportSubmitted) onReportSubmitted(res.data);
      onClose();
    } catch (e) {
      console.error("Submission error:", e);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[2000] bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
      <div className="glass-panel w-full max-w-2xl p-6 rounded-2xl border border-cyan-500/30 relative shadow-2xl">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-4">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-cyan-500/20 text-cyan-400 flex items-center justify-center font-bold">
              ⚠️
            </div>
            <div>
              <h2 className="text-base font-bold text-white">Submit Crowdsourced Citizen Disaster Report</h2>
              <p className="text-xs text-slate-400">Integrated with MoES Automated Spatial-Temporal Verification Engine</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Quick Location Preset Selector */}
        <div className="mb-4">
          <label className="text-xs text-slate-400 font-semibold block mb-1.5">Quick Location Presets:</label>
          <div className="flex flex-wrap gap-1.5">
            {CITY_PRESETS.map((city, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => handleCitySelect(city)}
                className={`text-[11px] px-2.5 py-1 rounded-lg border font-mono transition-all ${
                  formData.district === city.district
                    ? 'bg-cyan-500/20 border-cyan-400 text-cyan-300 font-bold'
                    : 'bg-slate-900/60 border-slate-800 text-slate-400 hover:text-white'
                }`}
              >
                📍 {city.name}
              </button>
            ))}
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1">Reporter Name / Alias</label>
              <input
                type="text"
                value={formData.reporterName}
                onChange={(e) => setFormData({ ...formData, reporterName: e.target.value })}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:border-cyan-400 focus:outline-none"
                required
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1">Disaster Category</label>
              <select
                value={formData.category}
                onChange={(e) => {
                  setFormData({ ...formData, category: e.target.value });
                  setPreviewResult(null);
                }}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:border-cyan-400 focus:outline-none font-bold"
              >
                <option value="FLOOD">🌊 FLOOD (Flash Flood / Inundation)</option>
                <option value="HEAVY_RAIN">🌧️ HEAVY_RAIN (Excessive Downpour)</option>
                <option value="CYCLONE_WIND">🌪️ CYCLONE_WIND (Destructive Gale)</option>
                <option value="HEATWAVE">🔥 HEATWAVE (Extreme Temperature)</option>
                <option value="HAILSTORM">🧊 HAILSTORM (Hail precipitation)</option>
                <option value="LIGHTNING">⚡ LIGHTNING (Severe thunderstorm)</option>
                <option value="BLIZZARD">❄️ BLIZZARD (Snowstorm)</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1">
                Reported Severity (Level {formData.severityLevel}/5)
              </label>
              <input
                type="range"
                min="1"
                max="5"
                value={formData.severityLevel}
                onChange={(e) => setFormData({ ...formData, severityLevel: Number(e.target.value) })}
                className="w-full h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-amber-400 mt-2"
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1">Latitude</label>
              <input
                type="number"
                step="0.0001"
                value={formData.latitude}
                onChange={(e) => setFormData({ ...formData, latitude: Number(e.target.value) })}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs font-mono text-cyan-300 focus:border-cyan-400 focus:outline-none"
                required
              />
            </div>

            <div>
              <label className="text-xs font-semibold text-slate-300 block mb-1">Longitude</label>
              <input
                type="number"
                step="0.0001"
                value={formData.longitude}
                onChange={(e) => setFormData({ ...formData, longitude: Number(e.target.value) })}
                className="w-full bg-slate-900 border border-slate-700 rounded-xl px-3 py-2 text-xs font-mono text-cyan-300 focus:border-cyan-400 focus:outline-none"
                required
              />
            </div>
          </div>

          <div>
            <label className="text-xs font-semibold text-slate-300 block mb-1">Detailed Observation</label>
            <textarea
              rows="2"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full bg-slate-900 border border-slate-700 rounded-xl p-3 text-xs text-slate-200 focus:border-cyan-400 focus:outline-none"
              placeholder="Describe road blockage, water level, wind damages..."
              required
            ></textarea>
          </div>

          {/* Instant Verification Dry-Run Preview Card */}
          {previewResult && (
            <div className="p-3.5 rounded-xl bg-slate-950/80 border border-cyan-500/30 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono text-cyan-400 font-bold flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-emerald-400" />
                  INSTANT VERIFICATION DRY-RUN PREVIEW
                </span>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                  previewResult.status === 'VERIFIED' ? 'badge-verified' :
                  previewResult.status === 'SUSPICIOUS' ? 'badge-suspicious' : 'badge-debunked'
                }`}>
                  {previewResult.status} ({previewResult.confidenceScore}%)
                </span>
              </div>
              <p className="text-xs text-slate-300">
                <strong>Reasoning:</strong> {previewResult.reasoning}
              </p>
              <div className="flex items-center justify-between text-[10px] font-mono text-slate-400 pt-1 border-t border-slate-900">
                <span>Matched Station: <strong>{previewResult.matchedStationName}</strong></span>
                <span>Distance: <strong>{previewResult.stationDistanceKm?.toFixed(1)} km</strong></span>
                <span>Latency: <strong>{previewResult.latencyMs} ms</strong></span>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex items-center justify-between pt-2 border-t border-slate-800">
            <button
              type="button"
              onClick={handleDryRunEvaluate}
              disabled={evaluating}
              className="btn-secondary text-xs"
            >
              <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
              {evaluating ? 'Analyzing Sensors...' : 'Instant Verification Dry-Run'}
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-400 hover:text-white"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="btn-primary text-xs"
              >
                <Send className="w-3.5 h-3.5" />
                {submitting ? 'Broadcasting to Kafka...' : 'Submit to Event Backbone'}
              </button>
            </div>
          </div>

        </form>

      </div>
    </div>
  );
}
