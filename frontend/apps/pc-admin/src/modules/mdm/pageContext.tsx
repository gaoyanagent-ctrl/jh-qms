import { createContext,useContext,type ReactNode } from 'react';
export interface MdmPageContext { module:'mdm'; pageType:'list'|'workspace'; objectType:string; routePath:string; visibleFields:string[]; availableActions:string[] }
const Context=createContext<MdmPageContext|null>(null); export const MdmPageContextProvider=({value,children}:{value:MdmPageContext;children:ReactNode})=><Context.Provider value={value}>{children}</Context.Provider>; export const useMdmPageContext=()=>useContext(Context);
