import { fireEvent, render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MdmModelDesignerPage } from './MdmModelDesignerPage';

const schema={id:2,domainCode:'manufacturing',code:'bomItem',name:'BOM行主数据',recordType:'MASTER',versionEnabled:true,effectiveDateEnabled:true,organizationScopeEnabled:false,approvalRequired:false,status:'DRAFT',currentModelVersion:1,uiSchema:{},fields:[{id:3,code:'componentMaterialCode',name:'组件物料',dataType:'REFERENCE',required:true,unique:false,readonly:false,searchable:true,sortable:false,listVisible:true,length:128,enumOptions:[],helpText:null,sortNo:10,referenceConfig:{targetModelCode:'material',valueFieldCode:'businessCode',displayFieldCode:'name',statusFieldCode:'lifecycleStatus',allowedStatuses:['ACTIVE']}}]};
const material={...schema,id:1,code:'material',name:'物料主数据',fields:[]};
vi.mock('./hooks',()=>({useMdmSchema:()=>({data:schema,isError:false}),useMdmModels:()=>({data:[material,schema]}),useSaveMdmModelDraft:()=>({mutate:vi.fn(),isPending:false}),usePublishMdmModel:()=>({mutate:vi.fn(),isPending:false})}));
vi.mock('../platform/roles/hooks',()=>({useRolesQuery:()=>({data:{records:[{id:1,roleName:'平台管理员',roleCode:'platform_admin',status:'ENABLED'}]},isLoading:false})}));

describe('MdmModelDesignerPage reference configuration',()=>{
  beforeEach(()=>vi.clearAllMocks());
  it('uses a labeled dialog instead of unlabeled controls inside the table',async()=>{
    render(<MemoryRouter initialEntries={['/mdm/models/bomItem/design']}><Routes><Route path="/mdm/models/:modelCode/design" element={<MdmModelDesignerPage/>}/></Routes></MemoryRouter>);
    const configure=await screen.findByRole('button',{name:'配置关联'});fireEvent.click(configure);
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByLabelText('关联模型')).toBeInTheDocument();
    expect(screen.getByLabelText('保存值字段')).toBeInTheDocument();
    expect(screen.getByLabelText('显示字段')).toBeInTheDocument();
    expect(screen.getByLabelText('状态字段')).toBeInTheDocument();
    expect(screen.getByLabelText('允许的状态')).toBeInTheDocument();
  });
  it('shows the model-level approval setting with an explanation',async()=>{
    render(<MemoryRouter initialEntries={['/mdm/models/bomItem/design']}><Routes><Route path="/mdm/models/:modelCode/design" element={<MdmModelDesignerPage/>}/></Routes></MemoryRouter>);
    expect(await screen.findByText('审批设置')).toBeInTheDocument();
    const checkbox=screen.getByRole('checkbox',{name:'记录生效需要审批'});expect(checkbox).toBeInTheDocument();
    expect(screen.getByText(/关闭后提交即直接生效/)).toBeInTheDocument();
    fireEvent.click(checkbox);
    expect(await screen.findByLabelText('审批角色')).toBeInTheDocument();
  });
});
