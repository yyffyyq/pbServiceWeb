import { reactive, ref } from 'vue';
import { getDutyConfig, saveBaseDate } from '@/api/index';

export function useDutyConfig() {
    const allConfigs = reactive({
        weekdayDutyList: [],
        saturdayGroup1: [],
        saturdayGroup2: [],
        monthEndDutyList: []
    });
    const baseDate = ref(new Date().toISOString().slice(0, 10));

    const loadAllConfigs = async () => {
        try {
            const res = await getDutyConfig();
            if (res.code === 0 && res.data) {
                const c = res.data;
                allConfigs.weekdayDutyList = c.weekdayDutyList || [];
                allConfigs.saturdayGroup1 = c.saturdayGroup1 || [];
                allConfigs.saturdayGroup2 = c.saturdayGroup2 || [];
                allConfigs.monthEndDutyList = c.monthEndDutyList || [];
                if (c.baseDate) baseDate.value = new Date(c.baseDate).toISOString().slice(0, 10);
            }
        } catch (e) {}
    };

    const updateBaseDate = async (newDate) => {
        if (!newDate) return false;
        const res = await saveBaseDate({ baseDate: newDate });
        if (res.code === 0) {
            baseDate.value = newDate;
            return true;
        }
        return false;
    };

    return { allConfigs, baseDate, loadAllConfigs, updateBaseDate };
}