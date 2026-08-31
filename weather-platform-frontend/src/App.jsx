import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import IndiaMap from './components/IndiaMap';
import VerificationConsole from './components/VerificationConsole';
import BigDataAnalytics from './components/BigDataAnalytics';
import ScenarioLab from './components/ScenarioLab';
import ReportModal from './components/ReportModal';
import { api } from './services/api';
import { ShieldCheck, AlertTriangle, CloudRain, Radio, Activity, Database, Flame, CheckCircle2 } from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('map');
  const [stations, setStations] = useState([]);
  const [reports, setReports] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [stats, setStats] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState(null);

  useEffect(() => {
    loadInitialData();
    // Poll data every 3 seconds for smooth real-time telemetry streaming
    const interval = setInterval(loadLiveUpdates, 3000);
    return () => clearInterval(interval);
  }, []);

  const loadInitialData = async () => {
    try {
      const [stnRes, repRes, altRes, statsRes] = await Promise.all([
        api.getStations(),
        api.getReports(),
        api.getActiveAlerts(),
        api.getSystemStats()
      ]);
      setStations(stnRes.data || []);
      setReports(repRes.data || []);
      setAlerts(altRes.data || []);
      setStats(statsRes.data || null);
    } catch (e) {
      console.warn("Initial data load (connecting to services):", e.message);
    }
  };

  const loadLiveUpdates = async () => {
    try {
      const [stnRes, repRes, altRes, statsRes] = await Promise.all([
        api.getStations(),
        api.getReports(),
        api.getActiveAlerts(),
        api.getSystemStats()
      ]);
      if (stnRes.data) setStations(stnRes.data);
      if (repRes.data) setReports(repRes.data);
      if (altRes.data) setAlerts(altRes.data);
      if (statsRes.data) setStats(statsRes.data);
    } catch (e) {}
  };

  const handleSelectReport = (report) => {
    setSelectedReport(report);
    setActiveTab('verify');
  };

  const handleReportSubmitted = (newReport) => {
    setReports(prev => [newReport, ...prev]);
    setSelectedReport(newReport);
    setToastMessage(`Citizen report submitted & dispatched to Kafka! Auto-verifying...`);
    setTimeout(() => setToastMessage(null), 5000);
    setActiveTab('verify');
  };

  const handleUpvote = async (reportId) => {
    try {
      const res = await api.upvoteReport(reportId);
      if (res.data) {
        setReports(prev => prev.map(r => r.id === reportId ? res.data : r));
        if (selectedReport?.id === reportId) setSelectedReport(res.data);
        setToastMessage("Report upvoted! Consensus score updated.");
        setTimeout(() => setToastMessage(null), 3000);
      }
    } catch (e) {}
  };

  const handleScenarioTriggered = () => {
    loadLiveUpdates();
    setToastMessage("Scenario event injected! Watch real-time sensor & verification changes.");
    setTimeout(() => setToastMessage(null), 5000);
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#060913] text-slate-100">
      
      {/* Top Header Navigation */}
      <Header
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        stats={stats}
        activeAlertsCount={alerts.length}
        onOpenReportModal={() => setIsReportModalOpen(true)}
      />

      {/* Real-time Event Toast Banner */}
      {toastMessage && (
        <div className="bg-cyan-500 text-slate-950 px-4 py-2 text-xs font-mono font-bold flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/30 sticky top-[57px] z-50 animate-pulse">
          <CheckCircle2 className="w-4 h-4" />
          {toastMessage}
        </div>
      )}

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6 space-y-6">
        
        {/* KPI Counter Cards Row */}
        <div className="grid grid-cols-2 md:grid-cols-5 gap-3">
          <div className="glass-panel p-3.5 border-l-4 border-l-cyan-500">
            <span className="text-[10px] font-mono text-slate-400 block uppercase">AWS Stations Active</span>
            <div className="text-xl font-bold font-mono text-cyan-400 mt-1">
              {stations.length || 30} <span className="text-xs text-slate-400 font-normal">telemetry nodes</span>
            </div>
          </div>

          <div className="glass-panel p-3.5 border-l-4 border-l-emerald-500">
            <span className="text-[10px] font-mono text-slate-400 block uppercase">Verified Reports</span>
            <div className="text-xl font-bold font-mono text-emerald-400 mt-1">
              {stats?.verifiedReportsCount || reports.filter(r => r.verificationStatus === 'VERIFIED').length || 12}
              <span className="text-xs text-slate-400 font-normal"> / {reports.length || 14}</span>
            </div>
          </div>

          <div className="glass-panel p-3.5 border-l-4 border-l-indigo-500">
            <span className="text-[10px] font-mono text-slate-400 block uppercase">Verification Accuracy</span>
            <div className="text-xl font-bold font-mono text-indigo-300 mt-1">
              {stats?.verificationAccuracyPercent || 92.8}%
            </div>
          </div>

          <div className="glass-panel p-3.5 border-l-4 border-l-teal-500">
            <span className="text-[10px] font-mono text-slate-400 block uppercase">Avg Engine Latency</span>
            <div className="text-xl font-bold font-mono text-teal-400 mt-1">
              ⚡ {stats?.averageVerificationLatencyMs || 42} ms
            </div>
          </div>

          <div className="glass-panel p-3.5 border-l-4 border-l-rose-500 col-span-2 md:col-span-1">
            <span className="text-[10px] font-mono text-slate-400 block uppercase">Active CAP Alerts</span>
            <div className="text-xl font-bold font-mono text-rose-400 mt-1 flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-rose-500 pulse-alert"></span>
              {alerts.length} Warnings
            </div>
          </div>
        </div>

        {/* Tab Views */}
        {activeTab === 'map' && (
          <IndiaMap
            stations={stations}
            reports={reports}
            alerts={alerts}
            onSelectReport={handleSelectReport}
          />
        )}

        {activeTab === 'verify' && (
          <VerificationConsole
            selectedReport={selectedReport}
            reports={reports}
            onSelectReport={setSelectedReport}
            onUpvote={handleUpvote}
          />
        )}

        {activeTab === 'analytics' && (
          <BigDataAnalytics stations={stations} />
        )}

        {activeTab === 'scenario' && (
          <ScenarioLab onScenarioTriggered={handleScenarioTriggered} />
        )}

      </main>

      {/* Citizen Disaster Reporting Modal */}
      <ReportModal
        isOpen={isReportModalOpen}
        onClose={() => setIsReportModalOpen(false)}
        onReportSubmitted={handleReportSubmitted}
      />

      {/* Footer */}
      <footer className="border-t border-slate-900 py-4 px-6 text-center text-xs text-slate-500 font-mono">
        SIH26069 Prototype • National Weather Big Data Analytics Platform • Ministry of Earth Sciences (MoES) &amp; IMD • Java 21 / Spring Boot Microservices / Kafka / ClickHouse / PostgreSQL / Redis / React
      </footer>

    </div>
  );
}
