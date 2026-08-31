import axios from 'axios';

const API_BASE = '/api/v1';

export const api = {
  // Ingestion & Stations
  getStations: () => axios.get(`${API_BASE}/stations`),
  ingestTelemetry: (event) => axios.post(`${API_BASE}/ingest/telemetry`, event),
  ingestSocial: (event) => axios.post(`${API_BASE}/ingest/social`, event),

  // Simulator & Scenarios
  getSimulatorStatus: () => axios.get(`${API_BASE}/simulator/status`),
  startSimulator: (rate = 20) => axios.post(`${API_BASE}/simulator/start?rate=${rate}`),
  stopSimulator: () => axios.post(`${API_BASE}/simulator/stop`),
  setSimulatorRate: (rate) => axios.post(`${API_BASE}/simulator/rate?rate=${rate}`),
  triggerScenario: (scenarioType) => axios.post(`${API_BASE}/simulator/trigger-scenario`, { scenarioType }),

  // Citizen Reports
  getReports: (status = '', state = '') => axios.get(`${API_BASE}/reports?status=${status}&state=${state}`),
  getReportById: (id) => axios.get(`${API_BASE}/reports/${id}`),
  submitReport: (report) => axios.post(`${API_BASE}/reports`, report),
  upvoteReport: (id) => axios.post(`${API_BASE}/reports/${id}/upvote`),

  // Verification
  evaluateReportDirectly: (report) => axios.post(`${API_BASE}/verify/evaluate`, report),
  getVerificationMetrics: () => axios.get(`${API_BASE}/verify/metrics`),

  // Analytics & Alerts
  getTimeSeries: (stationId = 'stn-mum-01', range = '24h') => axios.get(`${API_BASE}/analytics/timeseries?stationId=${stationId}&range=${range}`),
  getDistrictAnomalies: () => axios.get(`${API_BASE}/analytics/anomalies`),
  getActiveAlerts: () => axios.get(`${API_BASE}/alerts/active`),
  getSystemStats: () => axios.get(`${API_BASE}/analytics/system-stats`),
};
