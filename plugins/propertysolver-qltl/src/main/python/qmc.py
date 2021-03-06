'''
Created on 2019年8月1日

@author: chenyan
'''
import numpy as np
import sys
np.set_printoptions(threshold=np.inf)
from scipy.linalg.decomp_svd import orth
from scipy.constants.constants import epsilon_0
from scipy.linalg import null_space
from ast import literal_eval
from configparser import ConfigParser

np.set_printoptions(suppress=True)

verbose = False 

'''
COMMON
'''
def is_square(matrix):
    return len(matrix.shape) == 2 and matrix.shape[0] == matrix.shape[1]

def is_zero_array(a):
    zero_array = np.zeros(a.shape, dtype=np.complex)
    return array_equal(a, zero_array)

def array_equal(a1, a2):
    if a1.shape != a2.shape:
        return False
    diff = a1 - a2
    diff_real = np.abs(diff.real)
    diff_imag = np.abs(diff.imag)
    if len(np.argwhere(diff_real > epsilon_0)) != 0 or len(np.argwhere(diff_imag > epsilon_0)) != 0:
        return False
    return True

def complex_equal(c1, c2):
    diff = c1 - c2
    if np.abs(diff.real) > epsilon_0 or np.abs(diff.imag) > epsilon_0:
        return False
    return True


def is_positive(operator):
    if not is_square(operator):
        return False
    
    if array_equal(operator, np.zeros(operator.shape, dtype=np.complex)):
        return True
    
    eigen_values, _ = np.linalg.eig(operator)
    
    for value in eigen_values:
        if value.real < -epsilon_0:
            return False
    
    return True
        

def decompose_into_positive_operators(operator):
    if not is_square(operator):
        return
    dimension = operator.shape[0]
    real_part = operator.real
    image_part = operator.imag
    real_positive = np.matrix(np.zeros(shape=[dimension, dimension], dtype=np.complex))
    real_negative = np.matrix(np.zeros(shape=[dimension, dimension], dtype=np.complex))
    image_positive = np.matrix(np.zeros(shape=[dimension, dimension], dtype=np.complex))
    image_negative = np.matrix(np.zeros(shape=[dimension, dimension], dtype=np.complex))
    
    eigen_values, eigen_vectors = np.linalg.eig(real_part)

    for i in range(len(eigen_values)):
        if eigen_values[i] > epsilon_0:
            real_positive += eigen_values[i] * np.outer(eigen_vectors[:,i], np.conjugate(eigen_vectors[:,i]))
        elif eigen_values[i] < -epsilon_0:
            real_negative -= eigen_values[i] * np.outer(eigen_vectors[:,i], np.conjugate(eigen_vectors[:,i]))
    
    eigen_values, eigen_vectors = np.linalg.eig(image_part)
    for i in range(len(eigen_values)):
        if eigen_values[i] > epsilon_0:
            image_positive += eigen_values[i] * np.outer(eigen_vectors[:,i], np.conjugate(eigen_vectors[:,i]))
        elif eigen_values[i] < -epsilon_0:
            image_negative -= eigen_values[i] * np.outer(eigen_vectors[:,i], np.conjugate(eigen_vectors[:,i]))
    
    return real_positive, real_negative, image_positive, image_negative

def get_support(operator):
    if not is_square(operator):
        return
    dimension = operator.shape[0]
    
    orth_basis = np.matrix(orth(operator))
    
    res = np.matrix(np.zeros([dimension, dimension], dtype=np.complex))
    for i in range(orth_basis.shape[1]):
        m = operator * orth_basis[:,i]
        if not is_zero_array(m):
            res += np.outer(orth_basis[:,i], np.conjugate(orth_basis[:,i]))

    return res

def projector_join(projector1, projector2):
    return get_support(projector1 + projector2)

def get_orth_complement(projector):
    if not is_square(projector):
        return
    dimension = projector.shape[0]
    
    res = np.matrix(np.zeros([dimension, dimension], dtype=np.complex))
    basis = np.matrix(null_space(projector))
    for i in range(basis.shape[1]):
        if is_zero_array(projector * basis[:, i]):
            res += np.matrix(np.outer(basis[:, i], np.conjugate(basis[:, i])))
    
    return res


class SuperOperator:
    '''
    super operator
    '''
    
    def __init__(self, kraus=[], matrix_representation=None):
        '''
        Constructor
        kraus: list of np.matrix
        matrix_representation: np.matrix
        '''
        self._dimension = 0
        if len(kraus) != 0 and matrix_representation is not None:
            temp = np.matrix(np.zeros(shape=[self.dimension ** 2, self.dimension ** 2],dtype=np.complex))
            for i in range(len(kraus)):
                temp += np.kron(kraus[i], np.conjugate(kraus[i]))
            if not array_equal(temp, matrix_representation):
                print("kraus operators don't match matrix_representation!")
                return
        
        if len(kraus) != 0:
            self._dimension = kraus[0].shape[0]
            for m in kraus:
                if len(m.shape) != 2 or m.shape[0] != self._dimension or m.shape[1] != self._dimension:
                    print("Kraus operator's size is incorrect!")
                    return 
        
        if matrix_representation is not None:
            if not is_square(matrix_representation):
                print("Matrix is not a square!")
                return
            dimension = matrix_representation.shape[0]
            sqrt = np.sqrt(dimension)
    
            if sqrt - np.floor(sqrt) > epsilon_0:
                print("dimension is not a integer!")
                return
            self._dimension = int(np.floor(sqrt))
        self._kraus = kraus
        self._matrix_representation = matrix_representation
        
    @property
    def kraus(self):
        if self._matrix_representation is not None and len(self._kraus) == 0:
            choi_matrix = np.matrix(np.zeros([self._dimension ** 2, self._dimension ** 2], dtype=np.complex))
            for k in range(self._dimension):
                for n in range(self._dimension):
                    for m in range(self._dimension):
                        for j in range(self._dimension):
                            choi_matrix[k * self._dimension + m, n * self._dimension +j] = \
                            self._matrix_representation[k * self._dimension + n, m * self._dimension +j]
        
            eigen_values, eigen_vectors = np.linalg.eig(choi_matrix)    
            for i in range(len(eigen_values)):
                if eigen_values[i].real > epsilon_0:
                    eigen_vector = eigen_vectors[:,i] * np.sqrt(eigen_values[i].real)
                    if not is_zero_array(eigen_vector):
                        self._kraus.append(eigen_vector.reshape([self._dimension, self._dimension]))
              
        return self._kraus
    
    @property
    def dimension(self):
        return self._dimension
    
    @property
    def matrix_representation(self):
        if self._matrix_representation is None:
            res = np.matrix(np.zeros(shape=[self.dimension ** 2, self.dimension ** 2],dtype=np.complex))
            for i in range(len(self.kraus)):
                res += np.kron(self.kraus[i], np.conjugate(self.kraus[i]))
            return res
        else:
            return self._matrix_representation
    
    def get_dual_super_operator(self):
        kraus = self.kraus
        dual_kraus = []
        for operator in kraus:
            dual_kraus.append(np.matrix(operator).H)
        return SuperOperator(kraus=dual_kraus, matrix_representation=None)
    
    def get_positive_eigen_operators(self):
        '''
        Get a (complete, but not necessarily linear independent) set of 
        positive fixed-operators for the super-operator 
        '''
        res = []
        if self.dimension == 0 or len(self.kraus) == 0:
            return res

        matrix_representation = self.matrix_representation
        
        # eigen_values, eigen_vectors = np.linalg.eig(matrix_representation)
        eigen_values, eigen_vectors = np.linalg.eig(matrix_representation)
        
        for i in range(len(eigen_values)):
            if np.abs((eigen_values[i] - 1.0).real) < epsilon_0 and np.abs(eigen_values[i].imag) < epsilon_0:
                # eigen_vectors[:,i] shape (dimension, 1) is a matrix not a vector
                eigen_matrix = np.matrix(eigen_vectors[:,i].reshape([self.dimension, self.dimension]))
                
                real_positive, real_negative, image_positive, image_negative = decompose_into_positive_operators(eigen_matrix)
                
                if not is_zero_array(real_positive):
                    res.append(real_positive)
                if not is_zero_array(real_negative):
                    res.append(real_negative)
                if not is_zero_array(image_positive):
                    res.append(image_positive)
                if not is_zero_array(image_negative):
                    res.append(image_negative)
         
        return res
    
    def apply_on_operator(self, operator):
        if not is_square(operator):
            return     
        if not self.dimension == operator.shape[0]:
            return
        
        res = np.matrix(np.zeros([self.dimension, self.dimension], dtype=np.complex))
        
        for i in range(len(self.kraus)):
            res += self.kraus[i] * operator * self.kraus[i].H
        
        return res
    
    def product_super_operator(self, super_operator):
        new_matrix = self.matrix_representation * super_operator.matrix_representation
        return SuperOperator(kraus=[], matrix_representation=new_matrix)
    
    def product_operator(self, operator):
        kraus = []
        kraus.append(operator)
        super_operator = SuperOperator(kraus=kraus, matrix_representation=None)
        return super_operator.product_super_operator(self)
    
    def power(self, n):
        if n == 0:
            karus = []
            karus.append(np.eye(self.dimension, self.dimension, dtype=np.complex))
            return SuperOperator(kraus=karus, matrix_representation=None)
        elif n > 0:
            return self.product_super_operator(self.power(n - 1))
        
    def max_period(self):
        projects = self.bscc_decomposition()  
        periods = np.zeros(len(projects) - 1, dtype=np.int32)
        
        for i in range(1, len(projects)):
            periods[i - 1] = len(self.period_decomposition(projects[i]))
        
        return np.max(periods)
    
    def infinity(self):
        this_matrix = self.matrix_representation
        start = 15
        start_matrix = self.matrix_representation
        
        for _ in range(start):
            start_matrix = start_matrix * start_matrix
        
        max_period = self.max_period()
        for _ in range(1, max_period):
            start_matrix = start_matrix + start_matrix * this_matrix
        
        start_matrix = 1.0 / max_period * start_matrix
        previous_matrix = start_matrix
        
        n = 1
        while True:
            current_matrix =  n * previous_matrix * this_matrix + start_matrix
            current_matrix = 1.0 / (n + 1) * current_matrix
            if is_zero_array(previous_matrix - current_matrix):
                break
            previous_matrix = current_matrix
            ++n
        return SuperOperator(kraus=[], matrix_representation=current_matrix)
    
    def check_bscc(self, projector):
        support = get_support(self.apply_on_operator(projector))
        if not is_positive(projector - support):
            return False
        
        matrix_representation = np.kron(projector, np.conjugate(projector)) * self.matrix_representation
        
        pro_so = SuperOperator(kraus=[], matrix_representation=matrix_representation)
        fix_points = pro_so.get_positive_eigen_operators()
        
        if len(fix_points) != 1:
            return False
        
        return array_equal(fix_points[0], projector)

    def get_bscc(self, projector):
        matrix_representation = np.kron(projector, np.conjugate(projector)) * self.matrix_representation
        pro_so = SuperOperator(kraus=[], matrix_representation=matrix_representation)
        
        fix_points = pro_so.get_positive_eigen_operators()
        
        '''fix_points[i] compare to projector'''
        
        fix_points = sorted(fix_points, key=lambda x: np.linalg.matrix_rank(x))
        pop_index = []
        
        for i in range(len(fix_points) - 1):
            support_i = get_support(fix_points[i])
            for j in range(i + 1, len(fix_points)):
                if j in pop_index:
                    continue
                support_j = get_support(fix_points[j])
                if array_equal(fix_points[i], fix_points[j]) or is_positive(support_j - support_i):
                    pop_index.append(j)
        
        for index in pop_index:
            fix_points.pop(index)
    
        res = []
        
        if len(fix_points) == 0:
            return res
        elif len(fix_points) == 1:
            res.append(get_support(fix_points[0]))
            return res
        else:
            diff = fix_points[0] - fix_points[1]
            real_positive, real_negative, _, _ = decompose_into_positive_operators(diff)
            projector_s = None
            if is_zero_array(real_positive):
                projector_s = get_support(real_negative)
            else:
                projector_s = get_support(real_positive)
            complement = projector - projector_s
            
            res.extend(self.get_bscc(projector_s))
            res.extend(self.get_bscc(complement))
            return res
    
    def bscc_decomposition(self):
        dimension = self.dimension
        res = self.get_bscc(np.eye(dimension, dimension, dtype=np.complex))
        
        stationary = np.matrix(np.zeros([dimension, dimension], dtype=np.complex))
        for projector in res:
            stationary = projector_join(stationary, projector)
        
        res.insert(0, get_orth_complement(stationary))
        
        return res
        
    def period_decomposition(self, bscc):
        res = []
        
        if not self.check_bscc(bscc):
            return res
        
        so = self.product_operator(bscc)
        matrix_representation = so.matrix_representation
        
        eigen_values, _ = np.linalg.eig(matrix_representation)
        
        period = 0
        
        for i in range(len(eigen_values)):
            if np.abs((eigen_values[i] - 1.0).real) < epsilon_0 and np.abs(eigen_values[i].imag) < epsilon_0:
                period = period + 1
        
        return self.power(period).get_bscc(bscc)   
        
        
        
def create_from_choi_representation(matrix):
    if not is_square(matrix):
        print("Matrix is not a square!")
        return
    dimension = matrix.shape[0]
    sqrt = np.sqrt(dimension)
    
    if sqrt - np.floor(sqrt) > epsilon_0:
        print("Sqrt is not a integer!")
        return
    
    n_dimension = int(sqrt)
    
    kraus = []
    eigen_values, eigen_vectors = np.linalg.eig(matrix)
    
    for i in range(len(eigen_values)):
        if eigen_values[i].real > epsilon_0:
            eigen_vector = eigen_vectors[:,i] * np.sqrt(eigen_values[i].real)
            if not is_zero_array(eigen_vector):
                kraus.append(eigen_vector.reshape([n_dimension, n_dimension]))
    
    return SuperOperator(kraus=kraus, matrix_representation=None)

def pqmc_values(states, Q, pri):
    if len(Q) == 0:
        print("Q is null!")
        return
    
    super_operator_demension = list(Q.values())[0].dimension
    state_demension = np.max(states) + 1
    I_c = np.eye(state_demension, state_demension, dtype = np.complex)
    I_H = np.eye(super_operator_demension, super_operator_demension, dtype=np.complex)
    
    # compute epsilon_m
    epsilon_m = np.zeros([(super_operator_demension ** 2) * (state_demension ** 2), (super_operator_demension ** 2) * (state_demension ** 2)], dtype=np.complex)
    for key, value in Q.items():
        E_i_kraus = value.kraus
        adjacency_matrix = np.zeros([state_demension, state_demension], dtype=np.complex)
        adjacency_matrix[key[1], key[0]] = 1.0
        for e in E_i_kraus:
            E = np.kron(adjacency_matrix, e)
            epsilon_m += np.kron(E, np.conjugate(E))
    
    # compute epsilon_m_infinity
    if verbose:
        print("epsilon_m:")
        print(epsilon_m.shape)
    epsilon_m_so = SuperOperator(kraus=[], matrix_representation=epsilon_m)
    
    #compute P_even
    P_even = np.zeros([super_operator_demension * state_demension, super_operator_demension * state_demension], dtype=np.complex)
    bscc_min_pri_key = []
    bscc_min_pri_value = []
    B = epsilon_m_so.get_bscc(np.kron(I_c, I_H))
    for b in B:
        C_b = []
        vecs = orth(b)
        for j in range(vecs.shape[1]):
            vec = vecs[:, j]
            nonzero_index = -1
            flag = False
            for i in range(state_demension):
                if not is_zero_array(vec[i * super_operator_demension:(1 + i) * super_operator_demension]):
                    if not flag:
                        flag = True
                        nonzero_index = i
                    else:
                        flag = False
                        break
            if flag:
                C_b.append(pri[nonzero_index])
        bscc_min_pri_key.append(b)
        bscc_min_pri_value.append(np.min(C_b))
    EP = set()
    for key, value in pri.items():
        if value % 2 == 0:
            EP.add(value)
    for k in EP:
        P_k = np.zeros([super_operator_demension * state_demension, super_operator_demension * state_demension], dtype=np.complex)
        for i in range(len(bscc_min_pri_key)):
            if bscc_min_pri_value[i] == k:
                P_k += bscc_min_pri_key[i]
        P_even += P_k
    if verbose:
        print("P_even:")
        print(P_even)
        
    P_vec = np.zeros([(super_operator_demension ** 2) * (state_demension ** 2), 1], dtype=np.complex)
    
    for i in range(super_operator_demension):
        i_ket = np.matrix(I_H[i].reshape([super_operator_demension, 1]))
        for j in range(state_demension):
            s_ket = I_c[j].reshape([state_demension, 1])
            P_vec += np.kron(np.kron(s_ket, i_ket), np.kron(s_ket, i_ket))
    P_vec = np.dot(np.kron(np.kron(P_even, I_c), I_H), P_vec)
    if verbose:
        print("P_vec:")
        print(P_vec)
    
    eigen_values, eigen_vectors = np.linalg.eig(np.matrix(epsilon_m))
    Y = list()
    
    for i in range(len(eigen_values)):
        if complex_equal(eigen_values[i], 1.0 + 0.j):
            Y.append(np.array(eigen_vectors[:, i]).reshape([-1, ]))
    if verbose:
        print("Y")
        print(Y)
    
    v_ket = np.zeros([(super_operator_demension ** 2) * (state_demension ** 2), 1], dtype=np.complex)
    for i in range(len(Y)):
        coefficient = list()
        for j in range(len(Y)):
            if j != i:
                coefficient.append(Y[j])
        T_dual = np.array(np.matrix(epsilon_m).H - np.eye(epsilon_m.shape[0], epsilon_m.shape[1], dtype=np.complex))
        for k in range(len(T_dual)):
            coefficient.append(T_dual[k])
        x_prim_ket = np.array(null_space(np.array(coefficient))[:, 0]).reshape([-1, ])
        x_ket = x_prim_ket / np.inner(Y[i], x_prim_ket)
        v_ket += (np.inner(Y[i], P_vec.reshape([-1, ])) * x_ket).reshape([-1, 1])

    if verbose:
        print("v_ket:")
        print(v_ket)
        
    M = np.zeros([super_operator_demension * state_demension, super_operator_demension * state_demension], dtype=np.complex)
    for i in range(super_operator_demension):
        i_bra = I_H[i].reshape([1, super_operator_demension])
        for j in range(state_demension):
            s_bra = I_c[j].reshape([1, state_demension])
            M += np.kron(np.dot(np.dot(np.kron(np.kron(np.kron(I_c, I_H), s_bra), i_bra), v_ket), s_bra), i_bra)
    
    # M = epsilon_m_infinity_so.get_dual_super_operator().apply_on_operator(P_even)
    
    M = np.matrix(M)
    res = dict()
    for i in range(state_demension):
        E_s_ket = np.matrix(np.kron(I_c[i].reshape([state_demension, 1]), I_H))
        E_s_bra = np.matrix(np.kron(I_c[i].reshape([1, state_demension]), I_H))
        res[i] = E_s_bra * M * E_s_ket
    
    return res
    

def get_matrix_str(m):
    strs = []
    ma = np.asarray(m)
    row, col = ma.shape
    lines = []
    for ri in range(0, row):
        cs = []
        for ci in range(0, col):
            element = ma[ri][ci]
            cs.append("{}+{}j".format(element.real, element.imag))
        lines.append(",".join(cs))
    return "$".join(lines)
         
'''
states: int set
Q: qmc (int, int): superoperator
pri: state priority int: int
classical_state: int
super_operator_demension: int
'''
if __name__ == '__main__':
    cfg = ConfigParser()
    cfg.read(sys.argv[1], "utf8")
    args = cfg["args"]
    states = np.array(literal_eval(str(args["stateStr"])))
    Q = literal_eval(str(args["qStr"]))
    pri = literal_eval(str(args["priStr"]))
   
    Q_prim = dict()
    for key, value in Q.items():
        Q_prim[key] = SuperOperator(kraus=[], matrix_representation=np.array(value))
    Q = Q_prim
        
    Q_res = pqmc_values(states, Q, pri)
    for state in Q_res:
        print("{} : {}".format(state, get_matrix_str(Q_res[state])))
   
