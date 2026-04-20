import { RequestStatus } from './request-status.model';

export interface Request {
  id: number;
  title: string;
  description: string;
  status: RequestStatus;
  createdAt: string;
  code?: string;
  type?: string;
  priority?: string;
  creator?: string;
  assignee?: string;
}