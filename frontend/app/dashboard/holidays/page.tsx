'use client';

import { useEffect, useState } from 'react';
import DashboardLayout from '@/components/dashboard-layout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { holidayAPI } from '@/lib/api';
import { toast } from 'sonner';
import { Loader2, Plus, Pencil, Trash2, Calendar } from 'lucide-react';

const HOLIDAY_TYPES = ['NATIONAL', 'REGIONAL', 'COMPANY'];

const INDIAN_STATES = [
  'ALL',
  'ANDHRA_PRADESH', 'ARUNACHAL_PRADESH', 'ASSAM', 'BIHAR', 'CHHATTISGARH',
  'GOA', 'GUJARAT', 'HARYANA', 'HIMACHAL_PRADESH', 'JHARKHAND',
  'KARNATAKA', 'KERALA', 'MADHYA_PRADESH', 'MAHARASHTRA', 'MANIPUR',
  'MEGHALAYA', 'MIZORAM', 'NAGALAND', 'ODISHA', 'PUNJAB',
  'RAJASTHAN', 'SIKKIM', 'TAMIL_NADU', 'TELANGANA', 'TRIPURA',
  'UTTAR_PRADESH', 'UTTARAKHAND', 'WEST_BENGAL', 'DELHI'
];

interface Holiday {
  id: number;
  name: string;
  date: string;
  type: string;
  applicableStates: string;
  description?: string;
  active: boolean;
}

export default function HolidaysPage() {
  const [holidays, setHolidays] = useState<Holiday[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingHoliday, setEditingHoliday] = useState<Holiday | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    date: '',
    type: 'NATIONAL',
    state: null as string | null,
    active: true,
  });

  useEffect(() => {
    fetchHolidays();
  }, []);

  const fetchHolidays = async () => {
    try {
      setLoading(true);
      const response = await holidayAPI.getAllHolidays();
      if (response.data.success) {
        setHolidays(response.data.data);
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to fetch holidays');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      if (editingHoliday) {
        await holidayAPI.updateHoliday(editingHoliday.id, formData);
        toast.success('Holiday updated successfully!');
      } else {
        await holidayAPI.createHoliday(formData);
        toast.success('Holiday created successfully!');
      }
      setDialogOpen(false);
      resetForm();
      await fetchHolidays();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (holiday: Holiday) => {
    setEditingHoliday(holiday);
    setFormData({
      name: holiday.name,
      date: holiday.date,
      type: holiday.type,
      state: holiday.applicableStates === 'ALL' ? null : holiday.applicableStates,
      active: holiday.active,
    });
    setDialogOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this holiday?')) return;

    try {
      await holidayAPI.deleteHoliday(id);
      toast.success('Holiday deleted successfully!');
      await fetchHolidays();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to delete holiday');
    }
  };

  const resetForm = () => {
    setEditingHoliday(null);
    setFormData({
      name: '',
      date: '',
      type: 'NATIONAL',
      state: null,
      active: true,
    });
  };

  const handleDialogClose = (open: boolean) => {
    setDialogOpen(open);
    if (!open) {
      resetForm();
    }
  };

  const getTypeBadge = (type: string) => {
    const colors: { [key: string]: string } = {
      NATIONAL: 'bg-blue-100 text-blue-700',
      REGIONAL: 'bg-green-100 text-green-700',
      COMPANY: 'bg-purple-100 text-purple-700',
    };
    return (
      <Badge className={colors[type] || 'bg-gray-100 text-gray-700'}>
        {type}
      </Badge>
    );
  };

  const sortedHolidays = [...holidays].sort((a, b) =>
    new Date(a.date).getTime() - new Date(b.date).getTime()
  );

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Holidays</h1>
            <p className="text-gray-500 mt-1">Manage company holiday calendar</p>
          </div>
          <Dialog open={dialogOpen} onOpenChange={handleDialogClose}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Add Holiday
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>
                  {editingHoliday ? 'Edit Holiday' : 'Add New Holiday'}
                </DialogTitle>
                <DialogDescription>
                  {editingHoliday
                    ? 'Update holiday information'
                    : 'Create a new holiday in the calendar'}
                </DialogDescription>
              </DialogHeader>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Holiday Name</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) =>
                      setFormData({ ...formData, name: e.target.value })
                    }
                    placeholder="e.g., Independence Day"
                    required
                    disabled={submitting}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="date">Date</Label>
                  <Input
                    id="date"
                    type="date"
                    value={formData.date}
                    onChange={(e) =>
                      setFormData({ ...formData, date: e.target.value })
                    }
                    required
                    disabled={submitting}
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="type">Holiday Type</Label>
                    <Select
                      value={formData.type}
                      onValueChange={(value) =>
                        setFormData({ ...formData, type: value })
                      }
                      disabled={submitting}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {HOLIDAY_TYPES.map((type) => (
                          <SelectItem key={type} value={type}>
                            {type}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="state">State (Optional)</Label>
                    <Select
                      value={formData.state || 'ALL'}
                      onValueChange={(value) =>
                        setFormData({ ...formData, state: value === 'ALL' ? null : value })
                      }
                      disabled={submitting}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {INDIAN_STATES.map((state) => (
                          <SelectItem key={state} value={state}>
                            {state === 'ALL' ? 'All States' : state.replace(/_/g, ' ')}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="flex justify-end gap-4 pt-4">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => handleDialogClose(false)}
                    disabled={submitting}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={submitting}>
                    {submitting ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        {editingHoliday ? 'Updating...' : 'Creating...'}
                      </>
                    ) : editingHoliday ? (
                      'Update Holiday'
                    ) : (
                      'Create Holiday'
                    )}
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5" />
              Holiday Calendar
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
              </div>
            ) : holidays.length === 0 ? (
              <p className="text-center text-gray-500 py-8">
                No holidays found. Add your first holiday to get started.
              </p>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Date</TableHead>
                      <TableHead>Holiday Name</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Applicable States</TableHead>
                      <TableHead>Description</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sortedHolidays.map((holiday) => (
                      <TableRow key={holiday.id}>
                        <TableCell className="font-medium">
                          {new Date(holiday.date).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                          })}
                        </TableCell>
                        <TableCell>{holiday.name}</TableCell>
                        <TableCell>{getTypeBadge(holiday.type)}</TableCell>
                        <TableCell>
                          {holiday.applicableStates === 'ALL'
                            ? 'All States'
                            : holiday.applicableStates.replace(/_/g, ' ')}
                        </TableCell>
                        <TableCell className="max-w-xs truncate">
                          {holiday.description || '-'}
                        </TableCell>
                        <TableCell>
                          <Badge variant={holiday.active ? 'default' : 'secondary'}>
                            {holiday.active ? 'Active' : 'Inactive'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleEdit(holiday)}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleDelete(holiday.id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
