import { ref } from 'vue';
import { getDutyRecordsByDateRange } from '@/api/index';
import { isPublicHoliday, isAdjustedWorkday, isRestDay } from '@/utils/holiday';

export function useCalendar() {
    const currentDate = ref(new Date().toISOString().slice(0, 7));
    const dutyRecordsCache = ref({});

    // 替换原有的 isSingleWeek 方法
    // 替换原有的 isSingleWeek 方法（究极防暴毙版）
    const isSingleWeek = (targetDate, baseDateStr) => {
        // 1. 防御一：防止传进来的是 Vue 的 ref 对象而不是真实字符串
        let actualBaseDate = baseDateStr;
        if (actualBaseDate && actualBaseDate.value !== undefined) {
            actualBaseDate = actualBaseDate.value;
        }

        // 2. 防御二：如果完全没拿到数据，或者拿到的是字面量 "null"
        if (!actualBaseDate || actualBaseDate === 'null' || actualBaseDate === 'undefined') {
            return true; // 默认显示单周
        }

        // 3. 防御三：解决某些浏览器（特别是 Safari/iOS）不兼容带空格的日期字符串问题
        if (typeof actualBaseDate === 'string' && actualBaseDate.includes(' ')) {
            actualBaseDate = actualBaseDate.replace(' ', 'T');
        }

        const base = new Date(actualBaseDate);

        // 🚨 最核心的拦截：如果解析出来依然是 NaN (Invalid Date)，直接默认单周，
        // 否则后续算出的相差周数全是 NaN，导致角标永远全显示“双周”！
        if (isNaN(base.getTime())) {
            console.error('⚠️ [排班日历] 基准日期解析失败，收到的值是:', baseDateStr);
            return true;
        }

        // --- 下面是正常的对齐星期一计算逻辑 ---
        base.setHours(0, 0, 0, 0);
        const target = new Date(targetDate);
        target.setHours(0, 0, 0, 0);

        const baseDay = base.getDay() === 0 ? 7 : base.getDay();
        const baseMonday = new Date(base);
        baseMonday.setDate(base.getDate() - baseDay + 1);

        const targetDay = target.getDay() === 0 ? 7 : target.getDay();
        const targetMonday = new Date(target);
        targetMonday.setDate(target.getDate() - targetDay + 1);

        const diffTime = Math.abs(targetMonday.getTime() - baseMonday.getTime());
        const diffWeeks = Math.round(diffTime / (1000 * 60 * 60 * 24 * 7));

        return diffWeeks % 2 === 0;
    };

    const isMonthEndDay = (date) => {
        const day = date.getDate();
        const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
        return day === lastDay || day === lastDay - 1;
    };

    // 注意：移除了 allConfigs 参数，我们不再需要前端配置项来画饼了
    const generateCalendarDays = (baseDateStr) => {
        const year = parseInt(currentDate.value.split('-')[0]);
        const month = parseInt(currentDate.value.split('-')[1]) - 1;
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const firstDayWeek = firstDay.getDay();
        const daysInMonth = lastDay.getDate();

        const days = [];
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const createDayData = (date, isOtherMonth) => {
            const dayOfWeek = date.getDay();
            const isWeekendDay = isRestDay(date);
            const isHoliday = isPublicHoliday(date);
            const isAdjusted = isAdjustedWorkday(date);
            const isToday = date.getTime() === today.getTime();
            const isPastDate = date < today;
            const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

            // 1. 纯粹依赖后端接口返回的数据字典
            const recordsForDate = dutyRecordsCache.value ? dutyRecordsCache.value[dateStr] : null;

            // 2. 仅保留 UI 标签显示所需的单双周计算（仅用于在日历右上角显示个"单周"的角标，不参与人员计算）
            let weekType = null;
            if (dayOfWeek === 6) weekType = isSingleWeek(date, baseDateStr) ? '单周' : '双周';

            // 3. 核心修改：无情地把后端的 records 映射给前端，如果没有就是空！不再做任何兜底计算！
            let dutyPersons = [];
            if (recordsForDate && recordsForDate.length > 0) {
                dutyPersons = recordsForDate.map(r => ({
                    id: r.userId,
                    userName: r.userName,
                    dept: r.dept || '',
                    phone: r.phone || ''
                }));
            }

            // 4. 节假日强制清空（保留你原本的 UI 逻辑，如果这天被标记为节假日，就算后端有数据前端也不展示）
            if (isHoliday) dutyPersons = [];

            return {
                date,
                dateStr,
                isOtherMonth,
                isWeekend: isWeekendDay,
                isHoliday,
                isAdjusted,
                weekType,
                isToday,
                isPastDate,
                dutyPersons
            };
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