import React, { useState, useEffect } from 'react';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid, LineChart, Line, BarChart, Bar, Legend } from 'recharts';
import { Database, TrendingUp, AlertCircle, BarChart3, CloudRain, Wind, Thermometer } from 'lucide-react';
import { api } from '../services/api';

export default function BigDataAnalytics({ stations }) {
  const [selectedStation, setSelectedStation] = useState('stn-mum-01');
  const [timeSeries, setTimeSeries] = useState([]);
  const [anomalies, setAnomalies] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchAnalyticsData();
  }, [selectedStation]);

  const fetchAnalyticsData = async () => {
    setLoading(true);
    try {
      const [tsRes, anomRes] = await Promise.all([
        api.getTimeSeries(selectedStation, '24h'),
        api.getDistrictAnomalies()
      ]);
      setTimeSeries(tsRes.data || []);
      setAnomalies(anomRes.data || []);
    } catch (e) {
      console.error("Analytics fetch error:", e);
    } finally {
      setLoading(false);
    }
  };

  const formattedChartData = timeSeries.map((pt, idx) => ({
    time: pt.timestamp ? new Date(pt.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : `T-${24 - idx}h`,
    rain: pt.precipitationMm,
    histRain: pt.historicalPrecipitationAvgMm,
    temp: pt.temperature,
    wind: pt.windSpeedKmh,
    pressure: pt.pressure
  }));

  return (
    <div className="space-y-6">
      
      {/* Top Banner: ClickHouse Engine Status & Station Switcher */}
      <div className="glass-panel p-5 flex flex-col md:flex-row items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Database className="w-5 h-5 text-cyan-400" />
            <h2 className="text-base font-bold text-white">ClickHouse OLAP Time-Series Engine</h2>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
              MergeTree Partitioned
            </span>
          </div>
          <p className="text-xs text-slate-400">
            Sub-second analytical queries aggregated over millions of AWS sensor readings across India.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <label className="text-xs text-slate-400 font-semibold">Select Weather Station:</label>
          <select
            value={selectedStation}
            onChange={(e) => setSelectedStation(e.target.value)}
            className="bg-slate-900 border border-cyan-500/30 rounded-xl px-3 py-2 text-xs font-mono text-white focus:outline-none focus:border-cyan-400"
          >
            {stations.map((stn) => (
              <option key={stn.id} value={stn.id}>
                {stn.name} ({stn.state})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Grid of Big Data Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Chart 1: Precipitation vs Historical 30-Day Average */}
        <div className="glass-panel p-5 space-y-3">
          <div className="flex items-center justify-between border-b border-slate-800 pb-2.5">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <CloudRain className="w-4 h-4 text-cyan-400" />
              Precipitation (mm/h) vs Historical 30-Day Baseline
            </h3>
            <span className="text-[10px] font-mono text-cyan-400">24-HOUR WINDOW</span>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={formattedChartData}>
                <defs>
                  <linearGradient id="rainGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#00f0ff" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#00f0ff" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="histGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#64748b" stopOpacity={0.4}/>
                    <stop offset="95%" stopColor="#64748b" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="time" stroke="#64748b" tick={{ fontSize: 10 }} />
                <YAxis stroke="#64748b" tick={{ fontSize: 10 }} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', fontSize: '11px' }} />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                <Area type="monotone" dataKey="rain" name="Observed Rain (mm/h)" stroke="#00f0ff" fillOpacity={1} fill="url(#rainGrad)" />
                <Area type="monotone" dataKey="histRain" name="30-Day Avg Baseline" stroke="#94a3b8" strokeDasharray="4 4" fillOpacity={1} fill="url(#histGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Chart 2: Temperature & Wind Dynamics */}
        <div className="glass-panel p-5 space-y-3">
          <div className="flex items-center justify-between border-b border-slate-800 pb-2.5">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <Thermometer className="w-4 h-4 text-rose-400" />
              Surface Temperature (°C) &amp; Wind Speed (km/h)
            </h3>
            <span className="text-[10px] font-mono text-rose-400">TELEMETRY TREND</span>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={formattedChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="time" stroke="#64748b" tick={{ fontSize: 10 }} />
                <YAxis yAxisId="left" stroke="#ef4444" tick={{ fontSize: 10 }} />
                <YAxis yAxisId="right" orientation="right" stroke="#38bdf8" tick={{ fontSize: 10 }} />
                <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px', fontSize: '11px' }} />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
                <Line yAxisId="left" type="monotone" dataKey="temp" name="Temperature (°C)" stroke="#ef4444" strokeWidth={2.5} dot={false} />
                <Line yAxisId="right" type="monotone" dataKey="wind" name="Wind Speed (km/h)" stroke="#38bdf8" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>

      {/* District Meteorological Anomaly Matrix Table */}
      <div className="glass-panel p-5 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div className="flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-amber-400" />
            <h3 className="text-sm font-bold text-white">District Anomaly Index (Aggregated via ClickHouse)</h3>
          </div>
          <span className="text-xs text-slate-400 font-mono">Real-time extreme deviation detection</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="text-[11px] font-mono uppercase bg-slate-900/80 text-slate-400 border-b border-slate-800">
              <tr>
                <th className="p-3">District / State</th>
                <th className="p-3">Observed Metric</th>
                <th className="p-3">Historical Baseline</th>
                <th className="p-3">Anomaly Deviation</th>
                <th className="p-3">Alert Classification</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {anomalies.map((anom, idx) => (
                <tr key={idx} className="hover:bg-slate-900/40 transition-colors">
                  <td className="p-3 font-bold text-white">
                    {anom.district}, <span className="text-slate-400 font-normal">{anom.state}</span>
                  </td>
                  <td className="p-3 text-cyan-300 font-semibold">
                    {anom.currentRainfallMm ? `${anom.currentRainfallMm} mm/h rain` : `${anom.currentTemperature}°C temp`}
                  </td>
                  <td className="p-3 text-slate-400">
                    {anom.historicalAvgRainfallMm ? `${anom.historicalAvgRainfallMm} mm/h` : `${anom.historicalAvgTemp}°C`}
                  </td>
                  <td className="p-3">
                    <span className="text-rose-400 font-bold">
                      {anom.anomalyPercent > 0 ? `+${anom.anomalyPercent}%` : `${anom.anomalyPercent}%`}
                    </span>
                  </td>
                  <td className="p-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      anom.severity === 'EXTREME' ? 'badge-debunked' : 'badge-suspicious'
                    }`}>
                      {anom.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
}
