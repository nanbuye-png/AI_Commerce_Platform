import { create } from 'zustand';
interface SystemInfo { version: string; uptime: string; userCount: number; merchantCount: number; productCount: number; orderCount: number; aiCallCount: number; }
interface AdminInfo { id: string; username: string; role: string; }
interface AdminState { admin: AdminInfo | null; permissions: string[]; system: SystemInfo | null; setAdmin: (a: AdminInfo) => void; setPermissions: (p: string[]) => void; setSystem: (s: SystemInfo) => void; clear: () => void; }
const useAdminStore = create<AdminState>((set) => ({ admin: null, permissions: [], system: null, setAdmin: (admin) => set({ admin }), setPermissions: (permissions) => set({ permissions }), setSystem: (system) => set({ system }), clear: () => set({ admin: null, permissions: [], system: null }) }));
export default useAdminStore;