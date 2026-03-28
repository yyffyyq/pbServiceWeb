import { ref } from 'vue';
import { getDutyRecordsByDateRange } from '@/api/index';
import { isPublicHoliday, isAdjustedWorkday, isRestDay } from '@/utils/holiday';

export function useCalendar() {
    const currentDate = ref(new Date().toISOString().slice(0, 7));
    const dutyRecordsCache = ref({});

    // 100% 还原的单双周推演逻辑（含节假日跳过）
    const isSingleWeek = (date, baseDateStr) => {
        if (!baseDateStr) return true;
        const getSaturday = (d) => {
            const temp = new Date(d); temp.setHours(0, 0, 0, 0);
            temp.setDate(temp.getDate() + (6 - temp.getDay()));
            return temp;
        };
        const baseSat = getSaturday(baseDateStr);
        const targetSat = getSaturday(date);
        if (baseSat.getTime() === targetSat.getTime()) return true;

        let isSingle = true;
        let currentSat = new Date(baseSat);
        if (baseSat < targetSat) {
            while (currentSat < targetSat) {
                if (!isPublicHoliday(currentSat) && !isAdjustedWorkday(currentSat)) isSingle = !isSingle;
                currentSat.setDate(currentSat.getDate() + 7);
            }
        } else {
            while (currentSat > targetSat) {
                currentSat.setDate(currentSat.getDate() - 7);
                if (!isPublicHoliday(currentSat) && !isAdjustedWorkday(currentSat)) isSingle = !isSingle;
            }
        }
        return isSingle;
    };

    const isMonthEndDay = (date) => {
        const day = date.getDate();
        const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
        return day === lastDay || day === lastDay - 1;
    };

    const generateCalendarDays = (baseDateStr, allConfigs) => {
        const year = parseInt(currentDate.value.split('-')[0]);
        const month = parseInt(currentDate.value.split('-')[1]) - 1;
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const firstDayWeek = firstDay.getDay();
        const daysInMonth = lastDay.getDate();

        const days = [];
        const today = new Date(); today.setHours(0, 0, 0, 0);

        const createDayData = (date, isOtherMonth) => {
            const dayOfWeek = date.getDay();
            const isWeekendDay = isRestDay(date);
            const isHoliday = isPublicHoliday(date);
            const isAdjusted = isAdjustedWorkday(date);
            const isToday = date.getTime() === today.getTime();
            const isPastDate = date < today;
            const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

            const recordsForDate = dutyRecordsCache.value ? dutyRecordsCache.value[dateStr] : null;

            let dutyPersons = [];
            let weekType = null;
            if (dayOfWeek === 6) weekType = isSingleWeek(date, baseDateStr) ? '单周' : '双周';

            if (isPastDate) {
                dutyPersons = recordsForDate?.map(r => ({ id: r.userId, userName: r.userName, dept: r.dept || '', phone: r.phone || '' })) || [];
            } else {
                if (recordsForDate && recordsForDate.length > 0) {
                    dutyPersons = recordsForDate.map(r => ({ id: r.userId, userName: r.userName, dept: r.dept || '', phone: r.phone || '' }));
                } else {
                    let monthEndPersons = isMonthEndDay(date) ? (allConfigs.monthEndDutyList || []).map(c => ({ id: c.id, userName: c.userName, dept: c.dept || '', phone: c.phone || '' })) : [];
                    let basePersons = [];
                    if ((dayOfWeek >= 1 && dayOfWeek <= 5 && !isHoliday) || isAdjusted) {
                        basePersons = (allConfigs.weekdayDutyList || []).map(c => ({ id: c.id, userName: c.userName, dept: c.dept || '', phone: c.phone || '' }));
                    } else if ((dayOfWeek === 0 || dayOfWeek === 6) && !isHoliday && !isAdjusted && dayOfWeek === 6) {
                        basePersons = (isSingleWeek(date, baseDateStr) ? (allConfigs.saturdayGroup1 || []) : (allConfigs.saturdayGroup2 || [])).map(c => ({ id: c.id, userName: c.userName, dept: c.dept || '', phone: c.phone || '' }));
                    }
                    dutyPersons = [...basePersons, ...monthEndPersons];
                }
            }
            if (isHoliday) dutyPersons = [];

            return { date, dateStr, isOtherMonth, isWeekend: isWeekendDay, isHoliday, isAdjusted, weekType, isToday, isPastDate, dutyPersons };
        };

        for (let i = firstDayWeek - 1; i >= 0; i--) days.push(createDayData(new Date(year, month, -i), true));
        for (let day = 1; day <= daysInMonth; day++) days.push(createDayData(new Date(year, month, day), false));
        const remainingDays = 42 - days.length;
        for (let day = 1; day <= remainingDays; day++) days.push(createDayData(new Date(year, month + 1, day), true));

        return days;
    };

    const loadDutyRecords = async () => {
        try {
            const year = parseInt(currentDate.value.split('-')[0]);
            const month = parseInt(currentDate.value.split('-')[1]) - 1;
            const startDateStr = `${year}-${String(month + 1).padStart(2, '0')}-01`;
            const lastDay = new Date(year, month + 1, 0);
            const endDateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay.getDate()).padStart(2, '0')}`;

            const res = await getDutyRecordsByDateRange(startDateStr, endDateStr);
            if (res.code === 0 && res.data) {
                const recordsMap = {};
                res.data.forEach(record => {
                    const dStr = `${new Date(record.dutyDate).getFullYear()}-${String(new Date(record.dutyDate).getMonth() + 1).padStart(2, '0')}-${String(new Date(record.dutyDate).getDate()).padStart(2, '0')}`;
                    if (!recordsMap[dStr]) recordsMap[dStr] = [];
                    recordsMap[dStr].push(record);
                });
                dutyRecordsCache.value = recordsMap;
            }
        } catch (error) { dutyRecordsCache.value = {}; }
    };

    const prevMonth = async () => {
        const date = new Date(currentDate.value + '-01');
        date.setMonth(date.getMonth() - 1);
        currentDate.value = date.toISOString().slice(0, 7);
        await loadDutyRecords();
    };

    const nextMonth = async () => {
        const date = new Date(currentDate.value + '-01');
        date.setMonth(date.getMonth() + 1);
        currentDate.value = date.toISOString().slice(0, 7);
        await loadDutyRecords();
    };

    return { currentDate, dutyRecordsCache, generateCalendarDays, loadDutyRecords, prevMonth, nextMonth };
}