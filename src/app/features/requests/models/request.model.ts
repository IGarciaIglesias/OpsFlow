import { RequestStatus } from './request-status.model';

export type RequestPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export type RequestType = 'ACCESS' | 'INCIDENT' | 'CHANGE' | 'SUPPORT';

export interface Request {
  id: number;
  code: string;
  title: string;
  description: string;
  creator: string;
  assignee: string | null;
  priority: RequestPriority;
  type: RequestType;
  status: RequestStatus;
  createdAt: string;
}