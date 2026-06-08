export interface StageSummary {
  id: number;
  name: string;
  width: number;
  height: number;
}

export interface StageDetails extends StageSummary {
  solutionGrid: number[][];
}

export async function fetchStages(): Promise<StageSummary[]> {
  // Stub for TDD (Red phase)
  return [];
}

export async function fetchStageById(_id: number): Promise<StageDetails> {
  // Stub for TDD (Red phase)
  throw new Error('Not implemented');
}
