export interface DashboardSummary {
  totalRequests: number;
  draft: number;
  pendingValidation: number;
  validated: number;
  approved: number;
  inProgress: number;
  completed: number;
  failed: number;
  rejected: number;
  cancelled: number;
}