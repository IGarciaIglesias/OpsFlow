import { RequestStatus } from './request-status.model';

export interface Request {
  id: number;
  title: string;
  description: string;
  status: RequestStatus;
  createdAt: string;
}