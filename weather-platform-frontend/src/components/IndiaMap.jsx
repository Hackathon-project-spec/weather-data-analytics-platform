import React, { useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Circle, LayersControl } from 'react-leaflet';
import L from 'leaflet';
import { ShieldCheck, AlertTriangle, CloudRain, Wind, Thermometer, Radio, ExternalLink } from 'lucide-react';

// Custom Map Marker Icons using Leaflet DivIcons
const createAwsIcon = (temp, rain) => {
  const isHeavyRain = rain > 15;
  const isExtremeTemp = temp > 42;
  const color = isHeavyRain ? '#38bdf8' : isExtremeTemp ? '#ef4444' : '#00f0ff';
  
  return L.divIcon({
    className: 'custom-aws-marker',
    html: `
      <div style="
        background: rgba(8, 14, 28, 0.9);
        border: 2px solid ${color};
        box-shadow: 0 0 12px ${color};
        color: white;
        padding: 3px 6px;
        border-radius: 20px;
        font-family: 'JetBrains Mono', monospace;
        font-size: 10px;
        font-weight: bold;
        white-space: nowrap;
        display: flex;
        align-items: center;
        gap: 4px;
      ">
        <span style="width: 6px; height: 6px; border-radius: 50%; background: ${color}; display: inline-block;"></span>
        ${temp}°C | ${rain}mm
      </div>
    `,
    iconSize: [80, 24],
    iconAnchor: [40, 12]
  });
};

const createReportIcon = (status, category) => {
  let color = '#10b981'; // VERIFIED
  if (status === 'SUSPICIOUS') color = '#f59e0b';
  if (status === 'DEBUNKED') color = '#ef4444';
  if (status === 'PENDING') color = '#94a3b8';

  return L.divIcon({
    className: 'custom-report-marker',
    html: `
      <div style="
        background: ${color};
        color: #030712;
        padding: 4px 8px;
        border-radius: 8px;
        border: 1px solid white;
        box-shadow: 0 0 14px ${color};
        font-family: 'Outfit', sans-serif;
        font-size: 11px;
        font-weight: 800;
        text-transform: uppercase;
        display: flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;
      ">
        ⚠️ ${category}
      </div>
    `,
    iconSize: [90, 26],
    iconAnchor: [45, 13]
  });
};

export default function IndiaMap({ stations, reports, alerts, onSelectReport }) {
  const [showAws, setShowAws] = useState(true);
  const [showReports, setShowReports] = useState(true);
  const [showAlerts, setShowAlerts] = useState(true);

  return (
    <div className="relative w-full h-[640px] rounded-2xl overflow-hidden glass-panel border border-cyan-500/20">
      
      {/* Map Filter Controls Floating Bar */}
      <div className="absolute top-4 right-4 z-[500] bg-slate-900/90 backdrop-blur-md p-2 rounded-xl border border-slate-700 shadow-xl flex items-center gap-3 text-xs font-medium">
        <label className="flex items-center gap-1.5 cursor-pointer text-cyan-300 hover:text-white">
          <input
            type="checkbox"
            checked={showAws}
            onChange={(e) => setShowAws(e.target.checked)}
            className="rounded text-cyan-500 focus:ring-0"
          />
          AWS Stations ({stations.length})
        </label>
        <span className="text-slate-700">|</span>
        <label className="flex items-center gap-1.5 cursor-pointer text-emerald-300 hover:text-white">
          <input
            type="checkbox"
            checked={showReports}
            onChange={(e) => setShowReports(e.target.checked)}
            className="rounded text-emerald-500 focus:ring-0"
          />
          Citizen Reports ({reports.length})
        </label>
        <span className="text-slate-700">|</span>
        <label className="flex items-center gap-1.5 cursor-pointer text-rose-300 hover:text-white">
          <input
            type="checkbox"
            checked={showAlerts}
            onChange={(e) => setShowAlerts(e.target.checked)}
            className="rounded text-rose-500 focus:ring-0"
          />
          CAP Alert Zones ({alerts.length})
        </label>
      </div>

      {/* Main Leaflet Map */}
      <MapContainer
        center={[21.5, 80.0]}
        zoom={5}
        scrollWheelZoom={true}
        style={{ width: '100%', height: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://carto.com/">CARTO</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />

        {/* 1. Severe Disaster Warning Zones (CAP Polygons / Heat Circles) */}
        {showAlerts && alerts.map((alert) => (
          <Circle
            key={alert.alertId}
            center={[alert.centerLat, alert.centerLon]}
            radius={alert.radiusKm * 1000}
            pathOptions={{
              color: alert.severity === 'EXTREME' ? '#ef4444' : '#f59e0b',
              fillColor: alert.severity === 'EXTREME' ? '#ef4444' : '#f59e0b',
              fillOpacity: 0.22,
              weight: 2,
              dashArray: '6, 6'
            }}
          >
            <Popup>
              <div className="p-2 space-y-1.5">
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-400 border border-rose-500/40">
                    {alert.severity} ALERT
                  </span>
                  <span className="text-xs font-bold text-white">{alert.category}</span>
                </div>
                <h4 className="text-xs font-semibold text-slate-100">{alert.headline}</h4>
                <p className="text-[11px] text-slate-300">{alert.description}</p>
                <div className="text-[10px] text-amber-300 font-mono bg-amber-950/40 p-1.5 rounded border border-amber-500/20">
                  ⚠️ {alert.instruction}
                </div>
              </div>
            </Popup>
          </Circle>
        ))}

        {/* 2. Official Automatic Weather Stations (AWS) */}
        {showAws && stations.map((stn) => (
          <Marker
            key={stn.id}
            position={[stn.latitude, stn.longitude]}
            icon={createAwsIcon(stn.currentTemperature, stn.currentRainfallMm)}
          >
            <Popup>
              <div className="p-2 min-w-[200px] space-y-2">
                <div className="border-b border-slate-700 pb-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-cyan-400">{stn.code}</span>
                    <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-300">
                      {stn.stationType}
                    </span>
                  </div>
                  <h4 className="text-xs font-bold text-white">{stn.name}</h4>
                  <p className="text-[11px] text-slate-400">{stn.district}, {stn.state}</p>
                </div>
                <div className="grid grid-cols-2 gap-2 text-xs font-mono">
                  <div className="bg-slate-800/80 p-1.5 rounded">
                    <span className="text-slate-400 text-[10px]">Temperature:</span>
                    <div className="font-bold text-white">{stn.currentTemperature}°C</div>
                  </div>
                  <div className="bg-slate-800/80 p-1.5 rounded">
                    <span className="text-slate-400 text-[10px]">Rainfall:</span>
                    <div className="font-bold text-cyan-400">{stn.currentRainfallMm} mm/h</div>
                  </div>
                  <div className="bg-slate-800/80 p-1.5 rounded">
                    <span className="text-slate-400 text-[10px]">Wind:</span>
                    <div className="font-bold text-slate-200">{stn.currentWindSpeedKmh} km/h</div>
                  </div>
                  <div className="bg-slate-800/80 p-1.5 rounded">
                    <span className="text-slate-400 text-[10px]">Pressure:</span>
                    <div className="font-bold text-slate-200">{stn.currentPressure} hPa</div>
                  </div>
                </div>
              </div>
            </Popup>
          </Marker>
        ))}

        {/* 3. Crowdsourced Citizen Disaster Reports */}
        {showReports && reports.map((rep) => (
          <Marker
            key={rep.id}
            position={[rep.latitude, rep.longitude]}
            icon={createReportIcon(rep.verificationStatus, rep.category)}
            eventHandlers={{
              click: () => onSelectReport(rep)
            }}
          >
            <Popup>
              <div className="p-2 min-w-[220px] space-y-2">
                <div className="flex items-center justify-between border-b border-slate-700 pb-1.5">
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                    rep.verificationStatus === 'VERIFIED' ? 'badge-verified' :
                    rep.verificationStatus === 'SUSPICIOUS' ? 'badge-suspicious' :
                    rep.verificationStatus === 'DEBUNKED' ? 'badge-debunked' : 'badge-pending'
                  }`}>
                    {rep.verificationStatus} ({rep.confidenceScore || 0}%)
                  </span>
                  <span className="text-[10px] text-slate-400 font-mono">
                    Severity: {rep.severityLevel}/5
                  </span>
                </div>
                <div>
                  <h4 className="text-xs font-bold text-white">{rep.category} Disaster Report</h4>
                  <p className="text-[11px] text-slate-300 mt-1">{rep.description}</p>
                </div>
                {rep.verificationReasoning && (
                  <div className="text-[10px] bg-slate-950/70 p-1.5 rounded text-slate-300 border border-slate-800">
                    <strong>Reasoning:</strong> {rep.verificationReasoning}
                  </div>
                )}
                <div className="flex items-center justify-between pt-1">
                  <span className="text-[10px] text-slate-400 font-mono">
                    ⚡ {rep.verificationLatencyMs || 35}ms latency
                  </span>
                  <button
                    onClick={() => onSelectReport(rep)}
                    className="text-[11px] text-cyan-400 hover:text-cyan-300 font-medium flex items-center gap-1"
                  >
                    Inspect <ExternalLink className="w-3 h-3" />
                  </button>
                </div>
              </div>
            </Popup>
          </Marker>
        ))}

      </MapContainer>
    </div>
  );
}
