import { Request } from './request.model';

export const MOCK_REQUESTS: Request[] = [
  {
    id: 13,
    title: 'Cambio contrato 13',
    description: 'Renovación anual',
    status: 'REJECTED',
    createdAt: '2026-04-09T09:04:59Z',
  },
  {
    id: 14,
    title: 'Cambio contrato 14',
    description: 'Renovación anual',
    status: 'VALIDATED',
    createdAt: '2026-04-09T09:10:38Z',
  },
  {
    id: 15,
    title: 'Cambio contrato 15',
    description: 'Ampliación servicio',
    status: 'PENDING',
    createdAt: '2026-04-09T10:00:00Z',
  },
  {
    id: 16,
    title: 'Cambio contrato 16',
    description: 'Alta nuevo cliente',
    status: 'DRAFT',
    createdAt: '2026-04-09T10:05:00Z',
  },
];