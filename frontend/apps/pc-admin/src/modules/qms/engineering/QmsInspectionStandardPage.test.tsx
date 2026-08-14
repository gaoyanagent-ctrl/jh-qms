import type { QmsInspectionStandard } from '@iaf/domain-types';
import { useAuthStore } from '@iaf/auth';
import { QMS_PERMISSIONS } from '@iaf/permissions';
import { IafThemeProvider } from '@iaf/theme';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntApp } from 'antd';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { qmsEngineeringApi } from './api';
import { QmsInspectionStandardPage } from './QmsInspectionStandardPage';

vi.mock('./api',()=>({qmsEngineeringApi:{getInspectionStandard:vi.fn(),generateInspectionStandard:vi.fn(),updateInspectionStandard:vi.fn(),actOnInspectionStandard:vi.fn()}}));

const standard=(items=1):QmsInspectionStandard=>({
  id:8,standardNo:'IS-DWG-1',partId:2,drawingRevisionId:5,documentVersion:1,status:'DRAFT',approvalStatus:'NOT_SUBMITTED',sourceType:'AI',reactionPlan:null,version:1,updatedAt:'2026-08-13T00:00:00Z',submittedBy:null,submittedAt:null,approvedBy:null,approvedAt:null,releasedBy:null,releasedAt:null,approvalActions:[],
  items:Array.from({length:items},(_,index)=>({id:20+index,sequenceNo:index+1,category:'DIMENSION',itemName:`尺寸 ${index+1}`,requirement:'6.5±0.3',characteristicId:100+index,nominalValue:6.5,lowerLimit:6.2,upperLimit:6.8,specialCharacteristicCode:'B',supplierBatchSampling:'每批',supplierBatchMethod:'按图纸检验',supplierAnnualSampling:'每年一次',supplierAnnualMethod:'全尺寸检验',remark:null,sourceType:'AI',evidenceId:index+1,confidence:1,reviewStatus:'PENDING'}))
});

const renderPage=()=>render(<MemoryRouter initialEntries={['/qms/engineering/drawing-revisions/5/inspection-standard']}><QueryClientProvider client={new QueryClient({defaultOptions:{queries:{retry:false},mutations:{retry:false}}})}><IafThemeProvider><AntApp><Routes><Route path="/qms/engineering/drawing-revisions/:revisionId/inspection-standard" element={<QmsInspectionStandardPage/>}/></Routes></AntApp></IafThemeProvider></QueryClientProvider></MemoryRouter>);

describe('QmsInspectionStandardPage',()=>{
  beforeEach(()=>{vi.clearAllMocks();useAuthStore.setState({token:'token',principal:{tenantId:1,userId:1,username:'engineer',displayName:'Engineer',permissions:Object.values(QMS_PERMISSIONS)}});vi.mocked(qmsEngineeringApi.getInspectionStandard).mockResolvedValue(standard());});

  it('synchronizes an existing draft and immediately renders the returned list',async()=>{
    vi.mocked(qmsEngineeringApi.getInspectionStandard).mockReset().mockResolvedValueOnce(standard()).mockResolvedValue(standard(2));
    vi.mocked(qmsEngineeringApi.generateInspectionStandard).mockResolvedValue(standard(2));
    renderPage();
    fireEvent.click(await screen.findByRole('button',{name:'同步质量特性'}));
    await waitFor(()=>expect(qmsEngineeringApi.generateInspectionStandard).toHaveBeenCalledWith(5));
    expect(await screen.findByText('尺寸 2')).toBeInTheDocument();
  });

  it('saves the current draft before submitting it for approval',async()=>{
    const saved={...standard(),version:2,items:standard().items.map(item=>({...item,reviewStatus:'CONFIRMED'}))};
    const submitted={...saved,status:'APPROVING',approvalStatus:'PENDING'};
    vi.mocked(qmsEngineeringApi.updateInspectionStandard).mockResolvedValue(saved);
    vi.mocked(qmsEngineeringApi.actOnInspectionStandard).mockResolvedValue(submitted);
    renderPage();
    fireEvent.click(await screen.findByRole('button',{name:'提交审批'}));
    const dialog=await screen.findByRole('dialog');
    fireEvent.click(within(dialog).getByRole('button',{name:'OK'}));
    await waitFor(()=>expect(qmsEngineeringApi.updateInspectionStandard).toHaveBeenCalled());
    expect(qmsEngineeringApi.actOnInspectionStandard).toHaveBeenCalledWith(5,8,'submit-approval','');
    expect(vi.mocked(qmsEngineeringApi.updateInspectionStandard).mock.invocationCallOrder[0])
      .toBeLessThan(vi.mocked(qmsEngineeringApi.actOnInspectionStandard).mock.invocationCallOrder[0]);
  });
});
